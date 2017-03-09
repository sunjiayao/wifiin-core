package com.wifiin.kafka;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
/**
 * 维持着消息的类型
 * @author Running
 *
 */
public class KafkaTopicClassMap{
    private static Map<String,TopicClass> TOPIC_CLASS_MAP=Maps.newConcurrentMap();
    public static TopicClass getTopicClass(String topic){
        return TOPIC_CLASS_MAP.get(topic);
    }
    public static Class<?> getTopicClass(String topic,boolean isKey){
        TopicClass tc=getTopicClass(topic);
        return isKey?tc.keyClass:tc.valueClass;
    }
    public static TopicClass registerTopicClass(String topic,TopicClass topicClass){
        return TOPIC_CLASS_MAP.putIfAbsent(topic,topicClass);
    }
    public static TopicClass registerTopicClass(String topic,Class<?> keyClass,Class<?> valueClass){
        return registerTopicClass(topic,new TopicClass(keyClass,valueClass));
    }
    public static TopicClass registerTopicClass(Pattern topic,Class<?> keyClass,Class<?> valueClass){
        return registerTopicClass(topic.pattern(),keyClass,valueClass);
    }
    static void overrideTopicClass(String topic,Class<?> topicClass,boolean isKey){
        TOPIC_CLASS_MAP.compute(topic,(k,tc)->{
            if(tc==null){
                tc=new TopicClass();
            }
            if(isKey){
                tc.keyClass=topicClass;
            }else{
                tc.valueClass=topicClass;
            }
            return tc;
        });
    }
    
    public static class TopicClass{
        private Class<?> keyClass;
        private Class<?> valueClass;
        private TopicClass(){}
        public TopicClass(Class<?> keyClass,Class<?> valueClass){
            this.keyClass=keyClass;
            this.valueClass=valueClass;
        }
        public Class<?> getKeyClass(){
            return keyClass;
        }
        public Class<?> getValueClass(){
            return valueClass;
        }
    }
}
