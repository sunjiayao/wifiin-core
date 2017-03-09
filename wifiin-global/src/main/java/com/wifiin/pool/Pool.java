package com.wifiin.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.wifiin.pool.exception.NoAvailableObjectException;
import com.wifiin.util.ShutdownHookUtil;
/**
 * 对象池的一个简单实现
 * @author Running
 *
 * @param <T>
 */
public class Pool<T> implements PoolCommand<T>{
    private static final Logger log=LoggerFactory.getLogger(Pool.class);
    public static final String POOL_CONFIG_KEY_IN_CONFIG_MANAGER="pool.config";
    private final PoolConfig<T> config;
    private PooledObjectFactory<T> factory;
    /**
     * 池化对象队列
     */
    private final ArrayBlockingQueue<PooledObject<T>> idlePool;
    /**
     * 活跃对象集合，key是池化对象，值是池内维护池化对象的数据
     */
    private final Map<T,PooledObject<T>> activeObjects=Maps.newConcurrentMap();
    /**
     * 池统计数据
     */
    private Statistics statistics=new Statistics();
    /**
     * 退役线程池，定时执行
     */
    private ScheduledExecutorService retireExecutor;
    
    public Pool(PoolConfig<T> config){
        this.config=config;
        idlePool=Queues.newArrayBlockingQueue(maxIdle());
        populateIdle();
        retireExecutor=Executors.newScheduledThreadPool(1);
        retireExecutor.scheduleWithFixedDelay(new Runnable(){
            private int emptyRuns=0;
            public void run(){
                try{
                    if(idlePool.size()>config.getTestWhenIdleMoreThan() || (emptyRuns!=0 && emptyRuns%config.getRetrieveOnInvalidCount()==0)){
                        retireInvalidIdle();
                    }else{
                        emptyRuns++;
                    }
                }catch(Exception e){
                    log.warn("Pool.retireExecutor:",e);
                }
            }
        },config.getIdleTestPeriodMillis(),config.getIdleTestPeriodMillis(),TimeUnit.MILLISECONDS);
        shutdownHook();
    }
    public Pool(PoolConfig<T> config,PooledObjectFactory<T> factory){
        this(config);
        this.factory=factory;
    }
    /**
     * 指定池对象工厂
     * @param factory
     */
    public void setPooledObjectFactory(PooledObjectFactory<T> factory){
        this.factory=factory;
    }
    /**
     * 获取池化对象
     * @return
     */
    public T trieve(){
        PooledObject<T> pooled=null;
        boolean blocked=true;
        long start=System.currentTimeMillis();
        try{
            long blockMillis=config.getMaxBlockMillisOnExhausted();
            //小于零不等待，等于零一直等待，默认是零
            Triever<T> triever=null;
            if(blockMillis<0){
                blocked=false;
                triever=()->{
                    return idlePool.poll();
                };
            }else if(blockMillis==0){
                triever=()->{
                    return idlePool.take();
                };
            }else{
                triever=()->{
                    return idlePool.poll(config.getMaxBlockMillisOnExhausted(),TimeUnit.MILLISECONDS);
                };
            }
            return pooledObjectFactory().activate((pooled=trieve(triever)).getPooledObject());
        }finally{
            trievedStat(blocked,start,pooled);
        }
    }
    /**
     * 获取池化对象行为统计
     * @param blocked 获取时是否发生阻塞
     * @param startTrieve 开始获取的时间毫秒数
     * @param pooled 获取到的对象
     */
    private void trievedStat(boolean blocked,long startTrieve,PooledObject<T> pooled){
        pooled.setLastTrievedMillis(System.currentTimeMillis());
        PoolEventType.TRIEVED.execute(()->{
            //boolean firstHit,boolean blocked,long startTrieve,long lastTrievedMillis,long lastReturnedMillis,Object o
            statistics.trieveStat(pooled.getFirstHit(),blocked,startTrieve,pooled.getLastTrievedMillis(),pooled.getLastReturnedMillis(),pooled);
        });
    }
    /**
     * 验证获取到的对象
     * @param o 要验证的对象
     * @param test 如果是true就调用池对象工厂验证，否则只检查o是不是null
     * @return true:验证成功，false：难失败
     */
    private boolean validate(PooledObject<T> o,boolean test){
        return o!=null && (test ? pooledObjectFactory().validate(o.getPooledObject()) : true);
    }
    /**
     * 实际的获取池对象逻辑
     * @param triever 获取池对象的逻辑，参数决定是否阻塞等待，是否阻塞获取，是否池空就立即返回
     * @return 获取的池对象
     */
    private PooledObject<T> trieve(Triever<T> triever){
        PooledObject<T> trieved=null;
        try{
            trieved=trieveOnce();
        }catch(Exception e){
            throw new NoAvailableObjectException(e);
        }
        if(trieved!=null){
            return trieved;
        }
        return retrieve(triever);
    }
    /**
     * 首次不阻塞获取
     * @return
     */
    private PooledObject<T> trieveOnce(){
        PooledObject<T> trieved=idlePool.poll();
        if(trieved==null){
            populateIdle();
        }else{
            trieved=checkTrievedObject(trieved,true);
            if(trieved!=null){
                return trieved;
            }
        }
        return null;
    }
    /**
     * 重复获取池对象。trieveOnce什么也没返回就执行此方法
     * @param triever @see retrieve(Triever)
     * @return
     */
    private PooledObject<T> retrieve(Triever<T> triever){
        NoAvailableObjectException ex=null;
        PooledObject<T> retrieved=null;
        for(int i=0,c=config.getRetrieveOnInvalidCount();i<c && notFull();i++){
            try{
                retrieved=triever.trieve();
                if(retrieved==null){
                    populateIdle();
                }
            }catch(Exception e){
                if(ex==null){
                    ex=new NoAvailableObjectException();
                }
                ex.addSuppressed(e);
            }
            retrieved=checkTrievedObject(retrieved,false);
            if(retrieved!=null){
                return retrieved;
            }
        }
        //A B C 三部分必须是这个顺序
        retireInvalidIdle();//A
        //B START
        PooledObject<T> pooled=createOnTrive();//B
        if(pooled!=null){
            return pooled;
        }
        //B END
        populateIdle();//C
        if(ex==null){
            ex=new NoAvailableObjectException("nothing is lended from the pool");
        }
        throw ex;
    }
    /**
     * 检查池对象是否有效，无效的池对象立即在本方法销毁
     * @param pooled 池对象
     * @param firstHit 是否首次获取
     * @return 有效就返回带是否首次获取标志的pooled对象，无效返回null
     */
    private PooledObject<T> checkTrievedObject(PooledObject<T> pooled,boolean firstHit){
        if(validate(pooled,config.getTestOnTrieve())){
            pooled.setFirstHit(firstHit);
            activeObjects.put(pooled.getPooledObject(),pooled);
            return pooled;
        }else if(pooled!=null){
            destroy(pooled.getPooledObject());
        }
        return null;
    }
    /**
     * 获取池对象接口，它的实现决定具体采用哪种方式从池获取数据。现在有不阻塞获取，池空返回null；一直阻塞获取，没有就一直等待，阻塞一段时间获取，有立即返回，没有等待一段时间
     * @author Running
     *
     * @param <E>
     */
    private interface Triever<E>{
        public PooledObject<E> trieve() throws Exception;
    }
    /**
     * 如果池未满且未得到池对象是否新建
     * @return
     */
    private boolean createOnNoAvailableIfNotFull(){
        return config.getCreateOnNoAvailableIfNotFull();
    }
    /**
     * 空闲池对象是否太少
     * @return
     */
    private boolean tooFew(){
        return idleNotFull() && notFull();
    }
    /**
     * 池是否满
     * @return
     */
    private boolean notFull(){
        return currentTotal()<maxTotal();
    }
    /**
     * 最大池对象数
     * @return
     */
    private int maxTotal(){
        return config.getMaxTotal();
    }
    /**
     * 退役无效空闲对象
     */
    private void retireInvalidIdle(){
        PoolEventType.RETIRE.execute(()->{
            List<PooledObject<T>> list=new ArrayList<>(currentIdle()*2);
            while(idlePool.size()>0){
                PooledObject<T> pooled=idlePool.poll();
                validateAndOfferOrDestroy(pooled,false,false,list::add);
            }
            list.stream().forEach((p)->{
                validateAndOfferOrDestroy(p,true,false);
            });
        });
    }
    /**
     * 获取时如果没有有效池对象，且池未满，且配置允许在这种状况下新建就新建池对象
     * @return
     */
    private PooledObject<T> createOnTrive(){
        if(createOnNoAvailableIfNotFull()){
            PooledObjectFactory<T> factory=pooledObjectFactory();
            PooledObject<T> pooled=factory.createObject();
            return validateAndExecuteOrDestroy(pooled,config.getTestOnCreate(),false,(p)->{
                activeObjects.put(pooled.getPooledObject(),pooled);
                return pooled;
            }).get();
        }
        return null;
    }
    /**
     * 如果空闲对象太少，就填充空闲对象
     */
    private void populateIdle(){
        if(!tooFew()){
            return;
        }
        PoolEventType.TOO_FEW.execute(()->{
            PooledObjectFactory<T> factory=pooledObjectFactory();
            while(tooFew()){
                PooledObject<T> pooled=factory.createObject();
                validateAndOfferOrDestroy(pooled,config.getTestOnCreate(),false);
            }
        });
    }
    /**
     * 验证通过就把pooled填充到空闲队列，否则销毁，填充失败也销毁
     * @param pooled
     * @param test 是否使用池对象工厂验证，true:验证，false:只判断pooled是不是null
     * @return true:填充成功，false:失败
     */
    private boolean validateAndOfferOrDestroy(PooledObject<T> pooled,boolean test,boolean deactivate){
        return validateAndOfferOrDestroy(pooled,test,deactivate,this::offerOrDestroy);
    }
    /**
     * pooled验证通过就调用populate，否则就销毁
     * @param pooled
     * @param test 是否使用池对象工厂验证，true:验证，false:只判断pooled是不是null
     * @param populater 填充逻辑
     * @return
     */
    private boolean validateAndOfferOrDestroy(PooledObject<T> pooled,boolean test,boolean deactivate,Function<PooledObject<T>,Boolean> populater){
        return validateAndExecuteOrDestroy(pooled,test,deactivate,populater).orElse(false);
    }
    /**
     * pooled验证通过就执行executor，否则就销毁
     * @param pooled 
     * @param test  是否使用池对象工厂验证，true:验证，false:只判断pooled是不是null
     * @param executor 
     * @return
     */
    private <R> Optional<R> validateAndExecuteOrDestroy(PooledObject<T> pooled,boolean test,boolean deactivate,Function<PooledObject<T>,R> executor){
        if(!test || validate(pooled,test)){
            if(deactivate){
                this.pooledObjectFactory().deactivate(pooled.getPooledObject());
            }
            return Optional.ofNullable(executor.apply(pooled));
        }else{
            destroy(pooled);
        }
        return Optional.empty();
    }
    /**
     * 将参数填充到空闲队列，填充失败立即销毁
     * @param pooled 待填充队列
     * @return true:填充成功，false：填充失败
     */
    private boolean offerOrDestroy(PooledObject<T> pooled){
        if(idlePool.offer(pooled)){
            return true;
        }
        destroy(pooled);
        return false;
    }
    /**
     * 销毁pooled
     * @param pooled
     */
    private void destroy(PooledObject<T> pooled){
        destroy(pooled.getPooledObject());
    }
    /**
     * 销毁pooled
     * @param pooled
     */
    private void destroy(T pooled){
        pooledObjectFactory().destroy(pooled);
    }
    /**
     * 得到池对象工厂
     * @return
     */
    private PooledObjectFactory<T> pooledObjectFactory(){
        if(factory==null){
            synchronized(this){
                if(factory==null){
                    factory=config.getPooledObjectFactory();
                }
            }
        }
        return factory;
    }
    /**
     * 空闲队列是否已满
     * @return
     */
    private boolean idleNotFull(){
        return currentIdle()<maxIdle();
    }
    /**
     * 最大空闲对象数
     * @return
     */
    private int maxIdle(){
        return config.getMaxIdle();
    }
    /**
     * 当前池对象总数
     * @return
     */
    private int currentTotal(){
        return currentIdle()+currentActive();
    }
    /**
     * 当前空闲对象数
     * @return
     */
    private int currentIdle(){
        return idlePool.size();
    }
    /**
     * 当前活跃对象数
     * @return
     */
    private int currentActive(){
        return activeObjects.size();
    }
    /**
     * object返回池
     * @param object
     * @return true:返回成功，false:返回失败
     */
    public boolean returnObject(T object){
        if(object==null){
            return false;
        }
        PooledObject<T> pooled=activeObject(object);
        boolean valid=false;
        try{
            valid=returnObject(pooled);
            return valid;
        }finally{
            long returnedMillis=System.currentTimeMillis();
            pooled.setLastReturnedMillis(returnedMillis);
            final boolean validated=valid;
            PoolEventType.RETURNED.execute(()->{
                this.statistics.returnStat(returnedMillis-pooled.getLastTrievedMillis(),validated);
            });
        }
    }
    protected PooledObject<T> activeObject(T object){
        return activeObjects.remove(object);
    }
    /**
     * pooled返回池
     * @param pooled
     * @return
     */
    private boolean returnObject(PooledObject<T> pooled){
        return validateAndOfferOrDestroy(pooled,config.getTestOnReturn(),true);
    }
    /**
     * 用池对象作为参数执行fn
     * @param fn
     * @return fn的执行结果
     */
    public <R> R execute(Function<T,R> fn){
        T o=null;
        try{
            o=trieve();
            if(o!=null){
                return fn.apply(o);
            }
            throw new NoAvailableObjectException();
        }finally{
            returnObject(o);
        }
    }
    /**
     * 添加关闭池钩子，jvm退出前执行
     */
    private void shutdownHook(){
        ShutdownHookUtil.addHook(()->{
            shutdown();
        });
    }
    /**
     * 关闭池
     */
    public void shutdown(){
        retireExecutor.shutdownNow();
        closePooledObjects(idlePool);
        closePooledObjects(activeObjects.values());
    }
    /**
     * 关闭池对象
     * @param toShutdown 待关闭的池对象集合
     */
    private void closePooledObjects(Collection<PooledObject<T>> toShutdown){
        toShutdown.forEach((t)->{
            try{
                destroy(t.getPooledObject());
            }catch(Exception e){
                log.warn("Pool.closePooledObjects:",e);
            }
        });
    }
    protected void finalize() throws Throwable{
        try{
            this.shutdown();
        }catch(Exception e){
            log.warn("Pool.finalize",e);
        }
        super.finalize();
    }
    
}
