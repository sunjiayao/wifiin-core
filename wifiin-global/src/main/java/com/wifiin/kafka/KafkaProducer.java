package com.wifiin.kafka;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.wifiin.util.string.ThreadLocalStringBuilder;
/**
 * 对org.apache.kafka.clients.producer.KafkaProducer的薄封装。
 * 可以发送任意类型的消息
 * @author Running
 *
 */
public class KafkaProducer extends org.apache.kafka.clients.producer.KafkaProducer<Object,Object>{
    private static final Logger log=LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC_REPEATABLE_CONFIG=KafkaClient.TOPIC_REPEATABLE_CONFIG+".";
    private boolean topicRepeatable;
    private Map<String,Boolean> topicRepeatableMap=Maps.newConcurrentMap();
    /**
     * 构造生产者
     * @param properties 配置参数
     * @param keySerializer key的序列化器
     * @param valueSerializer value的序列化器
     */
    KafkaProducer(Map<String,Object> props){
        super(props);
        Object repeatable=props.get(KafkaClient.TOPIC_REPEATABLE_CONFIG);
        if(repeatable==null){
            topicRepeatable=false;
        }else if(repeatable instanceof Boolean){
            topicRepeatable=(Boolean)repeatable;
        }else if(repeatable instanceof String && (repeatable.equals("true") || repeatable.equals("false"))){
            topicRepeatable=Boolean.valueOf((String)repeatable);
        }else{
            topicRepeatable=false;
        }
    }
    private boolean topicRepetable(String topic){
        return topicRepeatableMap.computeIfAbsent(ThreadLocalStringBuilder.builder().append(TOPIC_REPEATABLE_CONFIG).append(topic).toString(),(k)->{
            try{
//                return ConfigManager.getInstance().getBoolean(k);
                return false;
            }catch(Exception e){
                log.warn("get topicRepeatable from ConfigManager:"+e);
                return topicRepeatable;
            }
        });
    }
    /**
     * 发送消息
     * @param topic 消息主题
     * @param partition 消息分区数，分区数越多，kafka越慢
     * @param key 消息key
     * @param value 消息体
     * @return 消息元数据
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public Future<RecordMetadata> send(String topic, int partition, Object key, Object value){
        KafkaTopicClassMap.registerTopicClass(topic,KafkaMessageKey.class,value.getClass());
        return super.send(new ProducerRecord<Object,Object>(topic,partition,new KafkaMessageKey(key,topicRepetable(topic)),value));
    }
    /**
     * @see send(String topic, int partition, Object key, Object value)
     * @param topic
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public Future<RecordMetadata> send(String topic, Object key, Object value){
        KafkaTopicClassMap.registerTopicClass(topic,KafkaMessageKey.class,value.getClass());
        return super.send(new ProducerRecord<Object,Object>(topic,new KafkaMessageKey(key,topicRepetable(topic)),value));
    }
    /**
     * @see send(String topic, int partition, Object key, Object value)
     * @param topic
     * @param value
     * @return
     */
    public Future<RecordMetadata> send(String topic, Object value){
        return send(topic,null,value);
    }
    /**
     * @see send(String topic, int partition, Object key, Object value)
     * @param topic
     * @param partition
     * @param key
     * @param value
     * @param callback 消息发送结果的回调
     * @return
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    public Future<RecordMetadata> send(String topic, int partition, Object key, Object value, Callback callback){
        KafkaTopicClassMap.registerTopicClass(topic,KafkaMessageKey.class,value.getClass());
        return super.send(new ProducerRecord<Object,Object>(topic,partition,new KafkaMessageKey(key,topicRepetable(topic)),value),callback);
    }
    /**
     * @see send(String topic, int partition, Object key, Object value, Callback callback)
     * @param topic
     * @param key
     * @param value
     * @param callback
     * @return
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    public Future<RecordMetadata> send(String topic, Object key, Object value, Callback callback){
        KafkaTopicClassMap.registerTopicClass(topic,KafkaMessageKey.class,value.getClass());
        return super.send(new ProducerRecord<Object,Object>(topic,new KafkaMessageKey(key,topicRepetable(topic)),value),callback);
    }
    /**
     * @see send(String topic, int partition, Object key, Object value, Callback callback)
     * @param topic
     * @param value
     * @param callback
     * @return
     */
    public Future<RecordMetadata> send(String topic, Object value, Callback callback){
        return send(topic,null,value,callback);
    }
}
