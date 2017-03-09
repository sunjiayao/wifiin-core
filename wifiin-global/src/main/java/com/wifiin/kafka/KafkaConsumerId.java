package com.wifiin.kafka;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.wifiin.kafka.exception.KafkaException;

/**
 * 消费者的标识，以订阅的主题为标识。
 * @author Running
 *
 * @param <T>
 */
public class KafkaConsumerId<T> implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-4621531036192192798L;
    T topics;
    private int hash;
    /**
     * 参数必须是Colection<String>或java.util.regex.Pattern，否则抛出com.wifiin.kafka.exception.KafkaException
     * @param topics
     * @throws KafkaException topics must be java.util.Collection or java.util.regex.Pattern
     */
    KafkaConsumerId(T topics){
        if(topics instanceof Collection || topics instanceof Pattern){
            this.topics=topics;
        }else{
            throw new KafkaException("topics must be java.util.Collection or java.util.regex.Pattern");
        }
    }
    public int hashCode(){
        if(hash==0){
            hash=new HashCodeBuilder().append(topics).toHashCode();
        }
        return hash;
    }
    public boolean equals(Object o){
        if(!(o instanceof KafkaConsumerId) || ((KafkaConsumerId)o).topics.getClass().equals(this.topics.getClass())){
            return false;
        }
        return new EqualsBuilder().append(this.hash,o.hashCode()).append(this.topics,((KafkaConsumerId)o).topics).isEquals();
    }
}
