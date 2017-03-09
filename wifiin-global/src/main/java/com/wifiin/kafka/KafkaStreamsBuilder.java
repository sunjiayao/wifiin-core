package com.wifiin.kafka;

import java.util.Map;
import java.util.Set;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.wifiin.config.ConfigManager;
import com.wifiin.util.ShutdownHookUtil;
/**
 * kafka流api的简单封装。
 * 按下面的例子完成流api的操作
 * KafkaStreamsBuilder builder=KafkaStreamsBuilder.newInstance();
 * builder.builder().stream("my-input-topic").forEach((k,v)->{...});//关键在这里，这里操作的是KStream对象，KStream对象有丰富的api可以实现复杂的流处理逻辑。k,v分别是Producer发布消息时指定的K V。
 * builder.start();
 * .....//do sth others
 * builder.close();
 * 本类创建的所有对象需要由本类关闭，无法从KafkaClient关闭
 * @author Running
 *
 */
public class KafkaStreamsBuilder{
    private static final Logger log=LoggerFactory.getLogger(KafkaStreamsBuilder.class);
    private static final Set<KafkaStreamsBuilder> ALL_BUILDERS=Sets.newConcurrentHashSet();
    private KStreamBuilder builder;
    private KafkaStreams streams;
    static{
        ShutdownHookUtil.addHook(()->{
            closeAll();
        });
    }
    private KafkaStreamsBuilder(){
        builder=new KStreamBuilder();
    }
    /**
     * 创建新实例
     * @return
     */
    public static KafkaStreamsBuilder newInstance(){
        KafkaStreamsBuilder ksb=new KafkaStreamsBuilder();
        ALL_BUILDERS.add(ksb);
        return ksb;
    }
    /**
     * 返回的对象用来构造流对象的行为
     * @see org.apache.kafka.streams.kstream.KStreamBuilder
     * @see org.apache.kafka.streams.kstream.KStream
     * @return
     */
    public KStreamBuilder builder(){
        return builder;
    }
    /**
     * 启动流对象
     * 初始化参数从ConfigManager得到
     * @see ConfigManager.getProperties(String)
     * @see KafkaClient.KAFKA_CONSUMER
     * Map的key @see org.apache.kafka.streams.StreamsConfig
     * @param kafkaStreamsKeys kafka stream初始化数据在@see com.wifiin.config.ConfigManager中对应的key，所有的HashMap都会被合并为一个，靠前的key对应的HashMap会覆盖后面的
     *                         不论是否指定本参数，一定会试图用@see KafkaClient.KAFKA_STREAMS_CONFIG作为key从@com.wifiin.config.ConfigManager获取HashMap作为配置参数。
     *                         @see KafkaClient.KAFKA_STREAMS_CONFIG默认是空的HashMap，本HashMap所有的键值对优先级最低，如果指定了其它的配置参数key，则本HashMap会被覆盖。
     */
    public void start(String... kafkaStreamsKeys){
        Map<String,Object> props=ConfigManager.getInstance().mergeHashMap(KafkaClient.mergeKeys(KafkaClient.KAFKA_STREAMS_CONFIG,kafkaStreamsKeys));
        start(props);
    }
    /**
     * 启动流对象
     * @param props 初始化参数
     */
    public void start(Map<String,Object> props){
        props.putIfAbsent(StreamsConfig.KEY_SERDE_CLASS_CONFIG, SerdeFactory.AutoSerde.class);
        props.putIfAbsent(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, SerdeFactory.AutoSerde.class);
        streams=new KafkaStreams(builder,new StreamsConfig(KafkaClient.populateClientId(props)));
        streams.start();
    }
    /**
     * 返回的对象可以做一些除了start close以外的其它操作
     * @return
     */
    public KafkaStreams streams(){
        return streams;
    }
    /**
     * 关闭流api
     */
    public void close(){
        streams.close();
        remove();
    }
    private void remove(){
        remove(this);
    }
    /**
     * 关闭所有曾经打开的流api
     */
    public static void closeAll(){
        ALL_BUILDERS.forEach((b)->{
            try{
                b.close();
            }catch(Exception e){
                log.warn("KafkaStreamsBuilder.closeAll:",e);
            }
        });
    }
    /**
     * 将本类的一个实例从本类实例集合中移除，移除后本类不再管理对象的生命周期和相关资源的释放
     * @param builder
     */
    public static void remove(KafkaStreamsBuilder builder){
        ALL_BUILDERS.remove(builder);
    }
}
