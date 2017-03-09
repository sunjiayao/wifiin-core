package com.wifiin.kafka;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.config.ConfigManager;
import com.wifiin.kafka.KafkaTopicClassMap.TopicClass;
import com.wifiin.kafka.exception.KafkaException;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
import com.wifiin.util.net.Localhost;
import com.wifiin.util.process.ProcessUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;
/**
 * 对kafka-clients的薄封装
 * @author Running
 *
 */
public class KafkaClient{
    /**
     * kafka broker 格式为host1:port1,host2:port2
     */
    public static final String BOOTSTRAP_SERVERS="bootstrap.servers";
    /**
     * producer consumer通用配置
     */
    public static final String KAFKA_CLIENT_CONFIG="kafka.client";
    /**
     * kafka producer的配置key，配置值从ConfigManager.getProperties(String)获取
     */
    public static final String KAFKA_PRODUCER_CONFIG="kafka.producer";
    /**
     * kafka consumer的配置key，配置值从ConfigManager.getProperties(String)获取
     */
    public static final String KAFKA_CONSUMER_CONFIG="kafka.consumer";
    /**
     * kafka consumer一次消费的消息数的配置key，配置值从ConfigManager.getProperties(String)获取
     */
    public static final String KAFKA_CONSUMER_POLL_TIMEOUT="kafka.consumer.poll.timeout";
    /**
     * kafka streams的配置key，配置值从ConfigManager.getProperties(String)获取
     */
    public static final String KAFKA_STREAMS_CONFIG="kafka.streams";
    /**
     * 序列化与反序列化自动处理方式，取值可以有json/kryo/text，或@see SerdeFactory.SerdeAlgo的枚举值
     */
    public static final String KAFKA_SERDE_AUTO="kafka.serde.auto";
    /**
     * 客户端标识
     */
    private static final String CLIENT_ID="client.id";
    /**
     * 是否异步执行消息处理器
     */
    public static final String ASYNC_EXECUTORS="kafka.async.executors";
    /**
     * 是否异步遍历消息处理器
     */
    public static final String ASYNC_EXECUTOR_SUBMIT="kafka.async.executor.submit";
    /**
     * 是否异步处理消息
     */
    public static final String ASYNC_CONSUMER_RECORDS="kafka.async.consumer.records";
    /**
     * 每一批消息处理结束是否提交
     */
    public static final String AUTO_COMMIT_AFTER_BATCH="kafka.auto.commit.after.batch";
    /**
     * 提交周期，单位是秒，默认是1秒
     */
    public static final String KAFKA_COMMIT_PERIOD_SECONDS="kafka.commit.period.seconds";
    /**
     * 消息处理器失败时自动重发的producer配置KEY数组或Collection
     */
    public static final String AUTO_REPEAT_SENDING_ON_CONSUMER_EXECUTOR_FAILURE="kafka.auto.repeat.sending.on.consumer.executor.failure";
    /**
     * 发送消息失败是否重发
     */
    public static final String TOPIC_REPEATABLE_CONFIG="kafka.topic.repeatable";
    /**
     * 消费者并行度配置
     */
    public static final String CONSUMER_PARALLISM="kafka.consumer.parallism";
    /**
     * 空字节数组
     */
    static final byte[] EMPTY_BYTES=new byte[0];
    /**
     * 当前进程内的kafka生产者
     */
    private static KafkaProducer producer;
    /**
     * 当前进程内的kafka消费者
     */
    private static Map<KafkaConsumerId<?>,KafkaConsumer> consumers;
    static{
        /**
         * 进程退出时关闭所有kafka生产者和消费者
         */
        ShutdownHookUtil.addHook(KafkaClient::close);
    }
    /**
     * 构造主题
     * @param app 应用系统名称，本方法会把名称全部转小写
     * @param module 应用模块名称
     * @param function 功能名，可以不传
     * @param others 其它业务数据部分
     * @return 生成的主题名
     */
    public static String generateTopic(String app,String module,String function,Object... others){
        StringBuilder builder=ThreadLocalStringBuilder.builder();
        builder.append(app.toUpperCase()).append(module);
        if(Help.isNotEmpty(function)){
            builder.append(function);
        }
        if(Help.isNotEmpty(others)){
            for(int i=0,l=others.length;i<l;i++){
                builder.append(others[i]);
            }
        }else{
            throw new IllegalArgumentException("parameter others must be specified, however it is empty");
        }
        return builder.toString();
    }
    /**
     * 如果没有指定client.id就自动生成一个，并填充到props。
     * 生成的client.id格式是IP_HEX-PROCESS_ID_HEX-THREAD_ID_HEX-currentTimeMillis_HEX-RANDOM_LONG_HEX
     * @param props
     * @return 传入的props
     */
    static Map<String,Object> populateClientId(Map<String,Object> props){
        props.computeIfAbsent(CLIENT_ID,(k)->{
            return KafkaClient.createId();
        });
        return props;
    }
    /**
     * 得到当前日期时间，精确到毫秒格式是yyyyMMddHHmmssSSS
     * @return
     */
    static long getCurrentDateTime(){
        Calendar now=Calendar.getInstance();
        long datetime=now.get(Calendar.YEAR);
        datetime=datetime*100+now.get(Calendar.MONTH)+1;
        datetime=datetime*100+now.get(Calendar.DATE);
        datetime=datetime*100+now.get(Calendar.HOUR_OF_DAY);
        datetime=datetime*100+now.get(Calendar.MINUTE);
        datetime=datetime*100+now.get(Calendar.SECOND);
        datetime=datetime*1000+now.get(Calendar.MILLISECOND);
        return datetime;
    }
    /**
     * 创建topic.id或消息Key
     * @return
     */
    static String createId(){
        return Localhost.getLocalIpInHex()+'-'+ProcessUtil.getPidHex()+'-'+Long.toHexString(Thread.currentThread().getId())+'-'+getCurrentDateTime();
    }
    public static String generateTopic(String app,String module,Object... others){
        return generateTopic(app,module,null,others);
    }
    /**
     * 将参数合并为一个字符串数组，新数组内仍然按others的顺序排列，last在最后一个
     * @param last 在新数组内的最后一个
     * @param others 在新数组内的顺序不变
     * @return
     */
    public static String[] mergeKeys(String last,String... others){
        String[] keys=new String[others.length+1];
        System.arraycopy(others,0,keys,0,others.length);
        keys[others.length]=last;
        return keys;
    }
    /**
     * 得到生产者，一个进程只创建一个生产者
     * 初始化参数从ConfigManager得到
     * @see ConfigManager.getProperties(String)
     * @see KafkaClient.KAFKA_CONSUMER
     * Properties的key @see org.apache.kafka.clients.producer.ProducerConfig
     * @param kafkaProducerKeys kafka producer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的HashMap都会被合并为一个，靠后的key对应的HashMap会覆盖前面的。
     *                          不论是否指定本参数，一定会试图用@see KafkaClient.KAFKA_PRODUCER_CONFIG作为key从@com.wifiin.config.ConfigManager获取HashMap作为配置参数。
     *                          @see KafkaClient.KAFKA_PRODUCER_CONFIG默认是空的HashMap，本HashMap所有的键值对优先级最低，如果指定了其它的配置参数key，则本HashMap会被覆盖。
     * @return
     */
    public static KafkaProducer producer(String... kafkaProducerKeys){
        if(producer==null){
            synchronized(KafkaProducer.class){
                if(producer==null){
                    Map<String,Object> props=ConfigManager.getInstance().mergeHashMap(mergeKeys(KAFKA_PRODUCER_CONFIG,kafkaProducerKeys));
                    producer(props);
                }
            }
        }
        return producer;
    }
    /**
     * 得到生产者，一个进程只创建一个生产者
     * @param props 配置参数
     * @return
     */
    public static KafkaProducer producer(Map<String,Object> props){
        if(producer==null){
            synchronized(KafkaProducer.class){
                if(producer==null){
                    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,SerdeFactory.AutoSerializer.class.getName());
                    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,SerdeFactory.AutoSerializer.class.getName());
                    producer=new KafkaProducer(populateClientId(props));
                }
            }
        }
        return producer;
    }
    /**
     * 得到消费者集合
     * @return
     */
    private static Map<KafkaConsumerId<?>,KafkaConsumer> consumers(){
        if(consumers==null){
            synchronized(KafkaConsumer.class){
                if(consumers==null){
                    consumers=Maps.newConcurrentMap();
                }
            }
        }
        return consumers;
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题
     * @param keyClass 消息key的类型，可以是null
     * @param valueClass 主题消息值的类型，可以是null
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的Properties都会被合并为一个，靠前的key对应的Properties会覆盖后面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(String topic,Class<?> keyClass,Class<?> valueClass,String...kafkaConsumerKeys){
        KafkaTopicClassMap.registerTopicClass(topic,keyClass,valueClass);
        return consumer(topic,kafkaConsumerKeys);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题
     * @param valueClass 主题消息值的类型，可以是null
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的Properties都会被合并为一个，靠前的key对应的Properties会覆盖后面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(String topic,Class<?> valueClass,String...kafkaConsumerKeys){
        return consumer(topic,KafkaMessageKey.class,valueClass,kafkaConsumerKeys);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题
     * @param keyClass 消息key的类型，可以是null
     * @param valueClass 主题消息值的类型，可以是null
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(String topic,Class<?> keyClass,Class<?> valueClass,Map<String,Object> props){
        KafkaTopicClassMap.registerTopicClass(topic,keyClass,valueClass);
        return consumer(topic,props);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题
     * @param valueClass 主题消息值的类型，可以是null
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(String topic,Class<?> valueClass,Map<String,Object> props){
        return consumer(topic,KafkaMessageKey.class,valueClass,props);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题，没有指定消息类型，会根据消息数据自动决定，自动支持list类型的json，map类型的json，kryo
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的Properties都会被合并为一个，靠前的key对应的Properties会覆盖后面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(String topic,String... kafkaConsumerKeys){
        return consumer(populateTopicCollection(topic),kafkaConsumerKeys);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,Map<String,Object> props)
     * @param topic 订阅的主题，没有指定消息类型，会根据消息数据自动决定，自动支持list类型的json，map类型的json，kryo
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(String topic,Map<String,Object> props){
        return consumer(populateTopicCollection(topic),props);
    }
    private static List<String> populateTopicCollection(String... topic){
        if(Help.isEmpty(topic)){
            throw new KafkaException("no topic is specified");
        }
        List<String> topics=Lists.newArrayList();
        for(int i=0,l=topic.length;i<l;i++){
            topics.add(topic[i]);
        }
        return topics;
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题集合，集合元素是具体的主题名，集合元素是具体的主题名和主题消息类型
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的Properties都会被合并为一个，靠前的key对应的Properties会覆盖后面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(Map<String,TopicClass> topics,String... kafkaConsumerKeys){
        registerTopicClasses(topics);
        return consumer(topics.keySet(),kafkaConsumerKeys);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题集合，集合元素是具体的主题名，集合元素是具体的主题名和主题消息类型
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(Map<String,TopicClass> topics,Map<String,Object> props){
        registerTopicClasses(topics);
        return consumer(topics.keySet(),props);
    }
    /**
     * 将参数topics指定的TopicClass注册到KafkaTopicClassMap
     * @param topics
     */
    private static void registerTopicClasses(Map<String,TopicClass> topics){
        topics.entrySet().forEach((entry)->{
            KafkaTopicClassMap.registerTopicClass(entry.getKey(),entry.getValue());
        });
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题，没有指定消息类型，会根据消息数据自动决定，自动支持list类型的json，map类型的json，kryo
     * @param kafkaConsumerKeys
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys){
        return consumer(new KafkaConsumerId<Collection<String>>(topics),kafkaConsumerKeys);
    }
    /**
     * @see KafkaConsumer consumer(Collection<String> topics,String... kafkaConsumerKeys)
     * @param topic 订阅的主题，没有指定消息类型，会根据消息数据自动决定，自动支持list类型的json，map类型的json，kryo
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(Collection<String> topics,Map<String,Object> props){
        return consumer(new KafkaConsumerId<Collection<String>>(topics),props);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题，这个对象订阅的主题符合这个正则表达式
     * @param keyClass 消息key类型
     * @param valueClass 主题消息值类型
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的HashMap都会被合并为一个，靠后的key对应的HashMap会覆盖前面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(Pattern topics,Class<?> keyClass,Class<?> valueClass,String... kafkaConsumerKeys){
        KafkaTopicClassMap.registerTopicClass(topics,keyClass,valueClass);
        return consumer(new KafkaConsumerId<Pattern>(topics),kafkaConsumerKeys);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题，这个对象订阅的主题符合这个正则表达式
     * @param valueClass 主题消息值类型
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的HashMap都会被合并为一个，靠后的key对应的HashMap会覆盖前面的
     * @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys)
     * @return
     */
    public static KafkaConsumer consumer(Pattern topics,Class<?> valueClass,String... kafkaConsumerKeys){
        return consumer(topics,KafkaMessageKey.class,valueClass,kafkaConsumerKeys);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题，这个对象订阅的主题符合这个正则表达式
     * @param keyClass 消息key类型
     * @param valueClass 主题消息值类型
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(Pattern topics,Class<?> keyClass, Class<?> valueClass, Map<String,Object> props){
        KafkaTopicClassMap.registerTopicClass(topics,keyClass,valueClass);
        return consumer(new KafkaConsumerId<Pattern>(topics),props);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * @param topics 返回的消费者对象订阅的主题，这个对象订阅的主题符合这个正则表达式
     * @param valueClass 主题消息值类型
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @return
     */
    public static KafkaConsumer consumer(Pattern topics, Class<?> valueClass, Map<String,Object> props){
        return consumer(topics,KafkaMessageKey.class,valueClass,props);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * 初始化参数从ConfigManager得到
     * @see ConfigManager.getProperties(String)
     * @see KafkaClient.KAFKA_CONSUMER
     * Properties的key @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @param id
     * @param kafkaConsumerKeys kafka consumer初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的HashMap都会被合并为一个，靠后的key对应的HashMap会覆盖前面的。
     *                          不论是否指定本参数，一定会试图用@see KafkaClient.KAFKA_CONSUMER_CONFIG作为key从@com.wifiin.config.ConfigManager获取HashMap作为配置参数。
     *                          @see KafkaClient.KAFKA_CONSUMER_CONFIG默认是空的HashMap，本HashMap所有的键值对优先级最低，如果指定了其它的配置参数key，则本HashMap会被覆盖。
     * @return 如果已存在相应id的KafkaConsumer会把已存在的KafkaConsumer关闭，然后创建一个新的
     */
    @SuppressWarnings({"rawtypes"})
    private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys){
        Map<String,Object> props=ConfigManager.getInstance().mergeHashMap(mergeKeys(KAFKA_CONSUMER_CONFIG,kafkaConsumerKeys));
        return consumer(id,props);
    }
    /**
     * 得到com.wifiin.kaka.KafkaConsumer对象。本方法返回的消费者尚未启动，需要调用其它方法添加消息处理器，指定同步或异步模式，最后调用execute(...)启动消费任务线程。
     * 
     * @param id 
     * @param props 配置参数 @see org.apache.kafka.clients.consumer.ConsumerConfig
     * @param kafkaConsumerKeys @see private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,String... kafkaConsumerKeys){...}
     * @return 如果已存在相应id的KafkaConsumer会把已存在的KafkaConsumer关闭，然后创建一个新的
     */
    @SuppressWarnings("rawtypes")
    private static <T extends KafkaConsumerId> KafkaConsumer consumer(T id,Map<String,Object> props){
        return consumers().compute(id,(k,v)->{
            if(v!=null){
                v.close();
            }
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,SerdeFactory.AutoDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,SerdeFactory.AutoDeserializer.class);
            return new KafkaConsumer(populateClientId(props)).topics(id);
        });
    }
    /**
     * 取消订阅，并将消费者从消费者集合中删除
     * @param topics 主题集合是具体的主题名，用本集合创建的消费者取消订阅并销毁
     */
    public static void unsubscribe(Collection<String> topics){
        unsubscribe(new KafkaConsumerId<Collection<String>>(topics));
    }
    /**
     * 取消订阅，并将消费者从消费者集合中删除
     * @param topics 主题用正则表达式表示，用这个正则表达式创建的消费者会取消订阅并销毁
     */
    public static void unsubscribe(Pattern topics){
        unsubscribe(new KafkaConsumerId<Pattern>(topics));
    }
    /**
     * 取消订阅，并将消费者从消费者集合中删除，其它所有的取消订阅方法都依赖本方法
     * @param id 用id.topics创建的消费会取消订阅并销毁
     */
    @SuppressWarnings({"rawtypes"})
    static <T extends KafkaConsumerId> void unsubscribe(T id){
        KafkaConsumer consumer=removeConsumer(id);
        if(consumer!=null){
            consumer.unsubscribe();
        }
    }
    /**
     * 将指定消费者标识的消费者从消费者集合中删除
     * @param id
     * @return
     */
    @SuppressWarnings("rawtypes")
    static KafkaConsumer removeConsumer(KafkaConsumerId id){
        return consumers.remove(id);
    }
    /**
     * 关闭创建的生产者和所有消费者。本类不操作流api，所以本方法不关闭曾经创建过的流相关的对象，要关闭流对象应使用KafkaStreamsBuilder.close*()
     */
    public static void close(){
        if(producer!=null){
            producer.close();
        }
        if(consumers!=null){
            consumers.values().forEach(KafkaConsumer::close);
            consumers.clear();
        }
    }
    /**
     * 将指定主题的消息输出到标准输出
     * @param topics 每一个元素都是具体的主题名
     */
    public static void stdout(String... topics){
        stdout((Object)topics,(b)->{
            return b.stream((String[])topics);
        });
    }
    /**
     * 将指定主题的消息输出到标准输出
     * @param topics 表示主题的正则表达式
     */
    public static void stdout(Pattern topics){
        stdout((Object)topics,(b)->{
            return b.stream((Pattern)topics);
        });
    }
    /**
     * 将指定主题的消息输出到标准输出
     * @param topics 表示主题的正则表达式或具体主题名的字符串数组
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    private static void stdout(Object topics,Function<KStreamBuilder,KStream> streamBuilder){
        KafkaStreamsBuilder builder=KafkaStreamsBuilder.newInstance();
        streamBuilder.apply(builder.builder()).foreach((k,v)->{
            Object[] kv=new Object[2];
            kv[0]=k;
            kv[1]=v;
            try{
                System.out.println(GlobalObject.getJsonMapper().writeValueAsString(kv));
            }catch(JsonProcessingException e){
                e.printStackTrace();
            }
        });
        builder.start();
    }
    /**
     * main方法第一个参数的值
     * @author Running
     *
     */
    private static enum CMD_TYPE{
        /**
         * 要输出的消息主题按字符串数组传递
         */
        ARRAY {
            @Override
            public void sysout(String[] topics){
                KafkaClient.stdout(topics);
            }
        },
        /**
         * 要输出的消息主题按正则表达式传递，正则表达式是数组最后一个元素
         */
        REGEX {
            @Override
            public void sysout(String[] topics){
                KafkaClient.stdout(Pattern.compile(topics[topics.length-1]));
            }
        };
        public abstract void sysout(String[] topics); 
    }
    /**
     * 将命令行参数指定的主题消息输出到标准输出。
     * 可以这样执行：
     * com.wifiin.kafka.Kafkaclient array topic1 topic2 ...
     * com.wifiin.kafka.Kafkaclient regex TOPICS_REGEX
     * @param args
     */
    public static void main(String[] args){
        CMD_TYPE.valueOf(args[0].toUpperCase()).sysout(args);
    }
}
