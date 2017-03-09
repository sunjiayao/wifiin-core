package com.wifiin.pool;

import java.io.Serializable;

import com.wifiin.reflect.ClassForNameMap;
import com.wifiin.util.Help;

/**
 * PoolConfig
   trieve
      maxBlockMillis              blockWhenExhausted==true时最大等待毫秒数
      testOnCreate                是否验证新创建的对象有效
      retrieveOnInvalidCount      testOnTrieve==true，且验证失败时重复获取的次数，达到次数触发RETIRE
   return
       testOnReturn               是否验证返回的对象有效
   idle
       testWhenIdleMoreThan       空闲数大于等于此值时检查与idleTestPeriod配合使用，默认是minIdle
       idleTestPeriod             空闲检查周期，
       minIdle    借时判断
       maxIdle    回池时判断
   maxTotal   创建时判断
 * @author Running
 *
 */
public class PoolConfig<T> implements Cloneable,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-66734998453837988L;
    //trieve config start
    /**
     * @see blockWhenExhausted时的最大等待毫秒数，小于零不等待，等于零一直等待，默认是零
     */
    private long maxBlockMillisOnExhausted=0;
    /**
     * 如果池未满且未得到有效对象是否新建
     */
    private boolean createOnNoAvailableIfNotFull;
    /**
     * 是否验证得到的对象有效性，默认是false
     */
    private boolean testOnTrieve=false;
    /**
     * 是否验证新建对象的有效性，默认是false
     */
    private boolean testOnCreate=false;
    /**
     * testOnTrieve==true且验证对象有效性失败时，重复获取对象的次数，小于等于零表示不重复，默认是1，达到次数仍未获得对象触发RETIRE，此值不建议太大
     * 抛出@see com.wifiin.pool.exception.NoAvailableObjectException
     */
    private int retrieveOnInvalidCount=1;
    //trieve config end
    //return config start
    /**
     * 是否在对象返回时验证对象有效性，默认是false
     */
    private boolean testOnReturn=false;
    //return config end
    //idle config start
    /**
     * 
     * 空闲数大于等于此值时检查空闲连接有效性，默认是minIdle。与idleTestPeriodMillis配合使用。
     * 如果指定的值小于零表示不检查每次都触发RETIRE。
     * 指定一个很大的数，表示每次永远不检查也不触发RETIRE。
     */
    private long testWhenIdleMoreThan;
    /**
     * 空闲检查周期，默认是1000毫秒，测试数量大于等于retrieveOnInvalidCount时触发RETIRE。指定小于等于0表示表示不启动检查线程。
     */
    private long idleTestPeriodMillis=1000;
    /**
     * 最小空闲对象数，必须大于零且小于maxTotal，否则抛出IllegalArgumentException
     */
    private int minIdle;
    /**
     * 最大空闲对象数，必须大于等于minIdle且小于等于maxTotal,默认是maxTotal。如果maxIdle>maxTotal，则程序指定maxIdle==maxTotal，如果maxIdle小于minIdle，则程序指定maxIdle==minIdle
     */
    private int maxIdle;
    //idle config end
    /**
     * 最大对象数，maxTotal==空闲对象+活动对象，maxTotal必须大于minIdle，否则抛出IllegalArgumentException
     */
    private int maxTotal;
    /**
     * 池对象工厂对象，如果不指定会抛出IllegalArgumentException
     */
    private PooledObjectFactory<T> pooledObjectFactory;
    /**
     * 所有属性填充完成必须调用@see check()
     */
    public PoolConfig(){}
    /**
     * 返回前调用@see check()
     * @param maxBlockMillisOnExhausted
     * @param testOnTrieve
     * @param testOnCreate
     * @param retrieveOnInvalidCount
     * @param testOnReturn
     * @param testWhenIdleMoreThan
     * @param idleTestPeriodMillis
     * @param minIdle
     * @param maxIdle
     * @param maxTotal
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public PoolConfig(long maxBlockMillisOnExhausted,boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,boolean testOnCreate,
            int retrieveOnInvalidCount,boolean testOnReturn,long testWhenIdleMoreThan,long idleTestPeriodMillis,
            int minIdle,int maxIdle,int maxTotal,
            Class<PooledObjectFactory<T>> pooledObjectFactoryClass) throws InstantiationException, IllegalAccessException{
        this.maxBlockMillisOnExhausted=maxBlockMillisOnExhausted;
        this.createOnNoAvailableIfNotFull=createOnNoAvailableIfNotFull;
        this.testOnTrieve=testOnTrieve;
        this.testOnCreate=testOnCreate;
        this.retrieveOnInvalidCount=retrieveOnInvalidCount;
        this.testOnReturn=testOnReturn;
        this.testWhenIdleMoreThan=testWhenIdleMoreThan;
        this.idleTestPeriodMillis=idleTestPeriodMillis;
        this.minIdle=minIdle;
        this.maxIdle=maxIdle;
        this.maxTotal=maxTotal;
        this.setPooledObjectFactoryClass(pooledObjectFactoryClass);
        check();
    }
    public PoolConfig(long maxBlockMillisOnExhausted,boolean createOnNoAvailableIfNotFull,boolean testOnTrieve,boolean testOnCreate,
            int retrieveOnInvalidCount,boolean testOnReturn,long testWhenIdleMoreThan,long idleTestPeriodMillis,
            int minIdle,int maxIdle,int maxTotal,
            String pooledObjectFactoryClassName) throws InstantiationException, IllegalAccessException{
        this(maxBlockMillisOnExhausted,createOnNoAvailableIfNotFull, testOnTrieve, testOnCreate,
             retrieveOnInvalidCount, testOnReturn, testWhenIdleMoreThan, idleTestPeriodMillis,
             minIdle, maxIdle, maxTotal,
            ClassForNameMap.get(pooledObjectFactoryClassName));
    }
    /**
     * 构造完成后立即调用。检查参数是否符合要求，否则抛出IllegalArgumentException
     */
    public void check(){
        if(minIdle<=0 || maxTotal<=0 || maxTotal<=minIdle || pooledObjectFactory==null){
            throw new IllegalArgumentException("maxTotal must be larger than minTotal");
        }
        if(maxIdle>maxTotal){
            maxIdle=maxTotal;
        }
        if(maxIdle<minIdle){
            maxIdle=minIdle;
        }
    }
    
    public long getMaxBlockMillisOnExhausted(){
        return maxBlockMillisOnExhausted;
    }
    public void setMaxBlockMillisOnExhausted(long maxBlockMillisOnExhausted){
        this.maxBlockMillisOnExhausted=maxBlockMillisOnExhausted;
    }
    public boolean getCreateOnNoAvailableIfNotFull(){
        return createOnNoAvailableIfNotFull;
    }
    public void setCreateOnNoAvailableIfNotFull(boolean createOnNoAvailableIfNotFull){
        this.createOnNoAvailableIfNotFull=createOnNoAvailableIfNotFull;
    }
    public boolean getTestOnTrieve(){
        return testOnTrieve;
    }
    public void setTestOnTrieve(boolean testOnTrieve){
        this.testOnTrieve=testOnTrieve;
    }
    public boolean getTestOnCreate(){
        return testOnCreate;
    }
    public void setTestOnCreate(boolean testOnCreate){
        this.testOnCreate=testOnCreate;
    }
    public int getRetrieveOnInvalidCount(){
        return retrieveOnInvalidCount;
    }
    public void setRetrieveOnInvalidCount(int retrieveOnInvalidCount){
        this.retrieveOnInvalidCount=retrieveOnInvalidCount;
    }
    public boolean getTestOnReturn(){
        return testOnReturn;
    }
    public void setTestOnReturn(boolean testOnReturn){
        this.testOnReturn=testOnReturn;
    }
    public long getTestWhenIdleMoreThan(){
        return testWhenIdleMoreThan==0?minIdle:testWhenIdleMoreThan;
    }
    public void setTestWhenIdleMoreThan(long testWhenIdleMoreThan){
        this.testWhenIdleMoreThan=testWhenIdleMoreThan;
    }
    public long getIdleTestPeriodMillis(){
        return idleTestPeriodMillis;
    }
    public void setIdleTestPeriodMillis(long idleTestPeriodMillis){
        this.idleTestPeriodMillis=idleTestPeriodMillis;
    }
    public int getMinIdle(){
        return minIdle;
    }
    public void setMinIdle(int minIdle){
        this.minIdle=minIdle;
    }
    public int getMaxIdle(){
        return maxIdle;
    }
    public void setMaxIdle(int maxIdle){
        this.maxIdle=maxIdle;
    }
    public int getMaxTotal(){
        return maxTotal;
    }
    public void setMaxTotal(int maxTotal){
        this.maxTotal=maxTotal;
    }
    /**
     * 返回池对象工厂
     * @return
     */
    public PooledObjectFactory<T> getPooledObjectFactory(){
        return pooledObjectFactory;
    }
    /**
     * 指定池对象工厂
     * @param pooledObjectFactory
     */
    public void setPooledObjectFactory(PooledObjectFactory<T> pooledObjectFactory){
        this.pooledObjectFactory=pooledObjectFactory;
        pooledObjectFactory.setPoolConfig(this);
    }
    /**
     * 返回池对象工厂类
     * @return
     */
    @SuppressWarnings("unchecked")
    public <C extends PooledObjectFactory<T>> Class<C> getPooledObjectFactoryClass(){
        return pooledObjectFactory==null?null:(Class<C>)pooledObjectFactory.getClass();
    }
    /**
     * 指定池对象工厂类
     * @param pooledObjectFactoryClass
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void setPooledObjectFactoryClass(Class<PooledObjectFactory<T>> pooledObjectFactoryClass) throws InstantiationException, IllegalAccessException{
        if(pooledObjectFactoryClass==null){
            pooledObjectFactory=null;
        }else{
            pooledObjectFactory=pooledObjectFactoryClass.newInstance();
            pooledObjectFactory.setPoolConfig(this);
        }
    }
    /**
     * 返回池对象工厂类名
     * @return
     */
    public String getPooledObjectFactoryClassName(){
        return pooledObjectFactory==null?null:pooledObjectFactory.getClass().getName();
    }
    /**
     * 指定池对象工厂类名
     * @param pooledObjectFactoryClassName
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void setPooledObjectFactoryClassName(String pooledObjectFactoryClassName) throws InstantiationException, IllegalAccessException{
        if(Help.isEmpty(pooledObjectFactoryClassName)){
            pooledObjectFactory=null;
        }else{
            setPooledObjectFactoryClass(ClassForNameMap.get(pooledObjectFactoryClassName));
        }
    }
}
