package com.wifiin.kafka;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wifiin.common.GlobalObject;
import com.wifiin.kafka.exception.KafkaException;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
/**
 * 对org.apache.kafka.clients.consumer.KafkaConsumer的薄封装，支持任意类型的消息
 * @author Running
 */
@SuppressWarnings("rawtypes")
public class KafkaConsumer extends org.apache.kafka.clients.consumer.KafkaConsumer<KafkaMessageKey,Object>{
    private static final Logger log=LoggerFactory.getLogger(KafkaConsumer.class);
    /**
     * 消息处理器
     */
    private final Set<BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>>> executors=Sets.newConcurrentHashSet();
    /**
     * 是否异步执行消息处理器，默认是false。如果本值是false则遍历executor的线程与拉取消息的线程是同一个
     * @see asyncExecutorSubmit
     */
    private final AtomicBoolean asyncExecutors=new AtomicBoolean(false);
    /**
     * 是否异步执行每个消息处理器，如果asyncExecutors是true，而本值是false，则每次所有executor在一个线程内串行执行。
     * 如果asyncExecutors是false，而本值是true，则遍历executor的线程与拉取消息的线程是同一个，但是每个executor在单独的线程执行
     */
    private final AtomicBoolean asyncExecutorSubmit=new AtomicBoolean(false);
    /**
     * 每个处理器是否异步处理消息,默认是false
     */
    private final AtomicBoolean asyncConsumerRecords=new AtomicBoolean(false);
    /**
     * 是否在executor处理完一批消息前执行commit,默认是false
     */
    private final AtomicBoolean commitBeforeExecutorReturn=new AtomicBoolean(false);
    /**
     * 是否正在执行
     */
    private final AtomicBoolean executing=new AtomicBoolean(false);
    /**
     * 消费者是否打开或关闭
     */
    private final AtomicBoolean open=new AtomicBoolean(true);
    /**
     * 消费者并行度
     */
    private final int parallism;
    /**
     * 消费者线程。
     * 有一个线程拉取消息，如果asyncExecutors和asyncConsumerRecords都是false则只创建一个线程用来拉取消息；否则会创建parallism+1个线程，一个用来拉取消息，其余线程用来处理消息。
     */
    private volatile ThreadPoolExecutor consumerExecutor;
    /**
     * 提交线程
     */
    private volatile ExecutorService commitExecutor;
    /**
     * 待提交数据
     */
    private volatile AtomicReference<Map<TopicPartition,OffsetAndMetadata>> commitData;
    /**
     * 获取kafka消息的超时时间
     */
    private long poolTimeout;
    /**
     * 消费者配置参数
     */
    private final Map<String,Object> config;
    /**
     * 消费者标识，以订阅时指定的主题集合或主题正则表达式为标识
     * @see KafkaConsumerId
     */
    private KafkaConsumerId id;
    /**
     * 取消息的线程，consumer一旦启动就固定了
     */
    private Thread pollingThread;
    /**
     * 构造器
     * @param props 初始化消费者的参数
     */
    KafkaConsumer(Map<String,Object> props){
        super(props);
        this.config=props;
        asyncExecutors.set((boolean)Help.convert(props.get(KafkaClient.ASYNC_EXECUTORS),false));
        asyncExecutorSubmit.set((boolean)Help.convert(props.get(KafkaClient.ASYNC_EXECUTOR_SUBMIT),false));
        asyncConsumerRecords.set((boolean)Help.convert(props.get(KafkaClient.ASYNC_CONSUMER_RECORDS),false));
        parallism=(int)Help.convert(props.get(KafkaClient.CONSUMER_PARALLISM),Runtime.getRuntime().availableProcessors());
        poolTimeout=((Number)Help.convert(config.get(KafkaClient.KAFKA_CONSUMER_POLL_TIMEOUT),1000L)).longValue();
        shutdownHook();
    }
    /**
     * 关闭线程池钩子
     */
    private void shutdownHook(){
        ShutdownHookUtil.addHook(()->{
            if(consumerExecutor!=null){
                consumerExecutor.shutdown();
            }
            if(commitExecutor!=null){
                commitExecutor.shutdown();
                commitImmediately();
            }
        });
    }
    /**
     * 按照asyncExecutors和asyncConsumerRecords重新定义
     */
    public KafkaConsumer initConsumerExecutor(){
        int threadCount=1;
        if(asyncExecutors.get() || asyncConsumerRecords.get()){
            threadCount+=parallism;
        }
        synchronized(this){
            if(consumerExecutor!=null){
                consumerExecutor.shutdownNow();
            }
            consumerExecutor=new ThreadPoolExecutor(1,threadCount,60,TimeUnit.SECONDS,new SynchronousQueue<>(),new ThreadPoolExecutor.AbortPolicy());
        }
        return this;
    }
    private synchronized ThreadPoolExecutor consumerExecutor(){
        return consumerExecutor;
    }
    /**
     * 立即提交缓存的kafka消息，并清除缓存
     */
    private void commitImmediately(){
        Map<TopicPartition,OffsetAndMetadata> toCommit=commitData.getAndSet(Maps.newConcurrentMap());
        try{
            if(Help.isNotEmpty(toCommit)){
                super.commitSync(toCommit);
            }
        }catch(Exception e){
            try{
                log.warn("KafkaConsumer.commit:"+GlobalObject.getJsonMapper().writeValueAsString(toCommit));
            }catch(JsonProcessingException e1){}
        }
    }
    /**
     * 是否在executor返回前提交
     * @param commit
     */
    public KafkaConsumer commitBeforeExecutorReturn(boolean commit){
        this.commitBeforeExecutorReturn.set(commit);
        return this;
    }
    /**
     * 本消费者订阅的消息
     * @param id
     * @return 返回自身
     */
    public KafkaConsumer topics(KafkaConsumerId id){
        this.id=id;
        return this;
    }
    /**
     * 添加消息处理器
     * @param executors
     * @return 返回自身
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>>... executors){
        return addExecutors(Arrays.asList(executors));
    }
    /**
     * 添加消息处理器
     * @param executors
     * @return 返回自身
     */
    public KafkaConsumer addExecutors(Collection<BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>>> executors){
        this.executors.addAll(executors);
        return this;
    }
    /**
     * 添加支持重发的消息处理器。重发的producer配置从@see ConfigManager得到，key是@see KafkaClient.AUTO_REPEAT_SENDING_ON_CONSUMER_EXECUTOR_FAILURE
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>... executors){
        return addExecutors(repeatable,Arrays.asList(executors));
    }
    /**
     * 添加支持重发的消息处理器。重发的producer配置从@see ConfigManager得到，key是@see KafkaClient.AUTO_REPEAT_SENDING_ON_CONSUMER_EXECUTOR_FAILURE
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @throws KafkaExection 从ConfigManager得到的重发producer配置参数不是String String[] Collection<String>时抛出异常
     * @return
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,Collection<BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>> executors){
        Object repeatConfigKeys=config.get(KafkaClient.AUTO_REPEAT_SENDING_ON_CONSUMER_EXECUTOR_FAILURE);
        if(repeatConfigKeys instanceof String){
            return addExecutors((String)repeatConfigKeys,repeatable,executors);
        }else if(repeatConfigKeys instanceof String[]){
            return addExecutors((String[])repeatConfigKeys,repeatable,executors);
        }else if(repeatConfigKeys instanceof Collection){
            return addExecutors((Collection<String>)repeatConfigKeys,repeatable,executors);
        }
        throw new KafkaException("type of managed config beyond "+KafkaClient.AUTO_REPEAT_SENDING_ON_CONSUMER_EXECUTOR_FAILURE+" is not supported, only String String[] Collection<String> are legal.");
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(String[] repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>... executors){
        return addExecutors(repeatProducerConfigKeys,repeatable,executors);
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(String repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>... executors){
        return addExecutors(new String[]{repeatProducerConfigKeys},repeatable,executors);
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer addExecutors(Collection<String> repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>... executors){
        return addExecutors((String[])repeatProducerConfigKeys.toArray(),repeatable,executors);
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    public KafkaConsumer addExecutors(String repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,Collection<BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>> executors){
        return addExecutors(new String[]{repeatProducerConfigKeys},repeatable,executors);
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    public KafkaConsumer addExecutors(Collection<String> repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,Collection<BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>> executors){
        return addExecutors((String[])repeatProducerConfigKeys.toArray(),repeatable,executors);
    }
    /**
     * 添加支持重发的消息处理器
     * @param repeatProducerConfigKeys 重发的producer配置key
     * @param repeatable 决定是否继续重发的逻辑
     * @param executors 消费逻辑，<strong>NOTE:</strong> 只要有一个失败就会重发，所以必须把消费者实现为<b>幂等</b>的
     * @return
     */
    public KafkaConsumer addExecutors(String[] repeatProducerConfigKeys,Function<ConsumerRecord<KafkaMessageKey,?>,Boolean> repeatable,Collection<BiConsumer<ConsumerRecords,ConsumerRecord<KafkaMessageKey,?>>> executors){
        executors.forEach((e)->{
            this.executors.add((crs,cr)->{
                try{
                    e.accept(crs,cr);
                    if(this.commitBeforeExecutorReturn.get()){
                        TopicPartition tp=new TopicPartition(cr.topic(),cr.partition());
                        OffsetAndMetadata om=new OffsetAndMetadata(cr.offset());
                        Map m=Maps.newHashMap();
                        m.put(tp,om);
                        super.commitSync(m);
                    }
                }catch(Exception ex){
                    if(cr.key().getRepeatable() && repeatable.apply(cr)){
                        cr.key().incrRepeat();
                        KafkaClient.producer(repeatProducerConfigKeys).send(cr.topic(),cr.key(),cr.value());
                    }else{
                        throw ex;
                    }
                }
            });
        });
        return this;
    }
    /**
     * 删除消息处理器
     * @param executors
     * @return 返回自身
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer removeExecutors(BiConsumer<ConsumerRecords,ConsumerRecord<?,?>>... executors){
        return removeExecutors(Arrays.asList(executors));
    }
    /**
     * 删除消息处理器，如果没有消息处理可用，将自动停止消费消息
     * @param executors
     * @return 返回自身
     */
    public KafkaConsumer removeExecutors(Collection<BiConsumer<ConsumerRecords,ConsumerRecord<?,?>>> executors){
        this.executors.removeAll(executors);
        if(executors.isEmpty()){
            executing.set(false);
        }
        return this;
    }
    /**
     * 判断是否还有消息处理器可以处理本订阅者的消息
     * @return
     */
    public boolean isEmptyExecutors(){
        return executors.isEmpty();
    }
    /**
     * 消息处理器数量
     * @return
     */
    public int executorCount(){
        return executors.size();
    }
    /**
     * 修改asyncExecutors
     * @param value
     * @return
     */
    private KafkaConsumer setAsyncExecutors(boolean value){
        asyncExecutors.set(value);
        return this;
    }
    /**
     * 同步执行消费者处理器，从下一批poll(...)返回的消息开始生效
     * @return 返回自身
     */
    public KafkaConsumer syncExecutors(){
        return setAsyncExecutors(false);
    }
    /**
     * 异步执行将消费者处理器改为异步模式，从下一批poll(...)返回的消息开始生效
     * @return 返回自身
     */
    public KafkaConsumer asyncExecutors(){
        return setAsyncExecutors(true);
    }
    /**
     * 修改asyncExecutorSubmit
     * @param value
     * @return
     */
    private KafkaConsumer setAsyncExecutorSubmit(boolean value){
        asyncExecutorSubmit.set(value);
        return this;
    }
    /**
     * 同步遍历executors
     * @return
     */
    public KafkaConsumer syncExecutorSubmit(){
        return setAsyncExecutorSubmit(false);
    }
    /**
     * 异步遍历executors
     * @return
     */
    public KafkaConsumer asyncExecutorSubmit(){
        return setAsyncExecutorSubmit(true);
    }
    /**
     * 每个执行器同步处理拉取的消费
     * @return
     */
    public KafkaConsumer syncConsumerRecords(){
        asyncConsumerRecords.set(false);
        return this;
    }
    /**
     * 每个执行器异步处理拉取的消息
     * @return
     */
    public KafkaConsumer asyncConsumerRecords(){
        asyncConsumerRecords.set(true);
        return this;
    }
    /**
     * 订阅消息
     * @param listener @see org.apache.kafka.clients.consumer.ConsumerRebalanceListener
     * @return 返回自身
     * @throws KafkaException("topics subscribed must be Collection<String> or java.util.regex.Pattern")
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer subscribe(ConsumerRebalanceListener listener){
        if(id.topics instanceof Collection){
            super.subscribe((Collection<String>)id.topics,listener);
        }else if(id.topics instanceof Pattern){
            super.subscribe((Pattern)id.topics,listener);
        }else{
            throw new KafkaException("topics subscribed must be Collection<String> or java.util.regex.Pattern");
        }
        return this;
    }
    /**
     * 订阅消息
     * @return 返回自身
     * @throws KafkaException("to subscribe with java.util.regex.Pattern topics, must specified a org.apache.kafka.clients.consumer.ConsumerRebalanceListener, or topics subscribed must be Collection<String>")
     */
    @SuppressWarnings("unchecked")
    public KafkaConsumer subscribe(){
        if(id.topics instanceof Collection){
            super.subscribe((Collection<String>)id.topics);
            return this;
        }else{
            throw new KafkaException("to subscribe with java.util.regex.Pattern topics, must specified a org.apache.kafka.clients.consumer.ConsumerRebalanceListener, or topics subscribed must be Collection<String>");
        }
    }
    /**
     * 处理拉取到的消息
     * @param stream 消息处理器流
     * @param records 待处理的消息集
     * @param commiter 提交
     * @return
     */
    private void executorConsumer(ConsumerRecords<KafkaMessageKey,?> records){
        boolean asyncExecutors=this.asyncExecutors.get();
        if(asyncExecutors){
            executeWithRejection(records,null,()->{
                if(asyncExecutorSubmit.get()){
                    executeWithRejection(records,null,()->{
                        executeExecutor((e)->{
                            execute(e,records);
                        });
                    });
                }else{
                    executeExecutor((e)->{
                        execute(e,records);
                    });
                }
            }); 
        }else{
            executeExecutor((e)->{
                execute(e,records);
            });
        }
    }
    /**
     * 将参数包含的消息推回kafka
     * @param records
     */
    private void pushBack(ConsumerRecords<KafkaMessageKey,?> records,ConsumerRecord<KafkaMessageKey,?> record){
        if(record==null){
            records.forEach((r)->{
                pushBack(records,r);
            });
        }else{
            String topic=record.topic();
            KafkaMessageKey key=record.key();
            Object value=record.value();
            KafkaClient.producer(this.config).send(topic,key,value);
        }
    }
    /**
     * 执行runnable，如果runnable抛出了异常就把消息推回kafka
     * @param records
     * @param idx
     * @param runnable
     */
    private void executeWithRejection(ConsumerRecords<KafkaMessageKey,?> records,ConsumerRecord<KafkaMessageKey,?> record,Runnable runnable){
        boolean isPollingThread=pollingThread==Thread.currentThread();
        try{
            consumerExecutor().submit(()->{
                runnable.run();
                notifyConsumerExecutor();
            });
        }catch(Exception e){
            log.warn("KafkaConsumer.rejected:"+e);
            pushBack(records,record);
            if(isPollingThread){
                this.waitConsumerExecutor();
            }
        }finally{
            if(!isPollingThread){
                this.notifyConsumerExecutor();
            }
        }
    }
    private void executeExecutor(Consumer<BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>>> consumer){
        executors.forEach(consumer);
    }
    /**
     * 为records的每条消息执行executor
     * @param executor
     * @param records
     */
    private void execute(BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>> executor,ConsumerRecords<KafkaMessageKey,?> records){
        execute(executor,records,asyncConsumerRecords.get());
    }
    /**
     * 为records的每条消息执行executor，paralle==true是并行执行，否则是串行执行
     * @param executor
     * @param records
     * @param paralle
     */
    private void execute(BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>> executor,ConsumerRecords<KafkaMessageKey,?> records, boolean paralle){
        if(paralle){
            executeRecord(records,(rs,r)->{
                executeWithRejection(rs,r,()->{
                    executor.accept(rs,r);
                });
            });
        }else{
            executeRecord(records,(rs,r)->{
                executor.accept(rs,r);
            });
        }
    }
    
    private void executeRecord(ConsumerRecords<KafkaMessageKey,?> records,BiConsumer<ConsumerRecords<KafkaMessageKey,?>,ConsumerRecord<KafkaMessageKey,?>> consumer){
        records.forEach((r)->{
            consumer.accept(records,r);
        });
    }
    /**
     * @see execute(long)
     * 拉取并处理消息
     * @param pollTimeout 等待获取消息的超时时间，如果没有消息消费者就阻塞这么多毫秒数
     */
    private void executing(long pollTimeout){
        for(;open.get() && executing.get() && Help.isNotEmpty(executors);){
            try{
                ConsumerRecords<KafkaMessageKey,?> records=super.poll(pollTimeout);
                if(!records.isEmpty()){
                    executorConsumer(records);
                }
            }catch(Exception e){
                log.warn("KafkaConsumer.execute:",e);
            }
        }
    }
    /**
     * 启动消费线程，只要还有一个消息处理器这个方法就不能被再次调用，多次调用不会发生任何事情。
     * 如果没有消息处理器，不会启动消费线程。如果消息处理器被清空了，运行中的消费线程任务会停止；重新添加消息处理器需要再次调用本方法或execute()。
     * 消息处理器从数量从0开始添加，需要调用本方法或execute()，消息处理器数量从大于0的时候添加新处理不需要重复调用本方法或execute()。
     * 消费者在执行期间，如果某批消息的任意一个在处理时抛出了异常，这一批消息剩下的就不再执行，如果希望发生异常时继续执行应在消息处理器内部捕获异常。
     * 本类的对象不记录消息的offset相关信息，如果需要自己控制offset，应在消息处理器内部记录相关信息。
     * 对于进程重启、重置offset等行为，可能导致消息被重复消费，因此为了避免错误，消息处理器应实现为幂等的。
     * @param pollTimeout 等待获取消息的超时时间，如果没有消息消费者就阻塞这么多毫秒数
     */
    private void execute(long pollTimeout){
        if(Help.isNotEmpty(executors) && !executing.getAndSet(true)){
            consumerExecutor().submit(()->{
                pollingThread=Thread.currentThread();
                try{
                    executing(pollTimeout);
                }finally{
                    closing();
                }
            });
        }
    }
    /**
     * 使用从ConfigManager得到的消息数
     * @see execute(int pollSize)
     * @see KafkaClient.KAFKA_CONSUMER_POLL_TIMEOUT，默认是1000ms
     */
    public void execute(){
        execute(poolTimeout);
    }
    /**
     * 执行关闭的行为
     */
    private void closing(){
        if(!open.get()){
            try{
                super.commitSync();
            }catch(Exception e){
                log.warn("KafkaConsumer:commitSync:BeforeClosing:"+e);
            }
            try{
                super.close();
            }catch(Exception e){
                log.warn("KafkaConsumer:Closing:"+e);
            }
            consumerExecutor.shutdown();
        }
    }
    /**
     * 参数是org.apache.kafka.common.TopicPartition的集合，但是指定了范型编译不允许覆盖。
     * 一旦执行了assign(Collection<org.apache.kafka.common.TopicPartition>)，消费者就与创建时的意义不同了，因此本方法会把它从KafkaClient管理的消费者集合中删除。
     * 调用本方法后需要开发者自己维护本消费者生命周期和对消费者对象的其它状态
     */
    @SuppressWarnings({"unchecked"})
    public void assign(Collection partitions){
        KafkaClient.removeConsumer(id);
        super.assign(partitions);
    }
    /**
     * 退订消息，然后销毁consumer
     */
    public void unsubscribe(){
        super.unsubscribe();
        KafkaClient.removeConsumer(id).close();
    }
    /**
     * 关闭本消费者，本方法会等待当前正在处理的消息完成后彻底关闭，等待时间内不再接收新消息。关闭前提交offset
     */
    public void close(){
        open.set(false);
        super.wakeup();
    }
    private void waitConsumerExecutor(){
        waitOrNotifyConsumerExecutor((es)->{
            try{
                es.wait();
            }catch(Exception e){}
        });
    }
    private void notifyConsumerExecutor(){
        waitOrNotifyConsumerExecutor((es)->{
            try{
                es.notifyAll();
            }catch(Exception e){}
        });
    }
    private void waitOrNotifyConsumerExecutor(Consumer<ExecutorService> consumer){
        ExecutorService es=consumerExecutor();
        synchronized(es){
            consumer.accept(es);;
        }
    }
}
