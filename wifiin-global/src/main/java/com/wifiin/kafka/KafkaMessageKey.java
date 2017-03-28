package com.wifiin.kafka;

import java.io.Serializable;

/**
 * kafka消息key。
 * k:业务相关的属性
 * datetime:发消息时的毫秒数
 * repeat：重试次数，初始值是0
 * repeatable:是否在消费消息发生异常时可重试，默认是false不可重试
 * @author Running
 *
 * @param <K> 消息Key的类型
 */
public class KafkaMessageKey<K> implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=4386091467436415281L;
    private K k;
    private long datetime;
    private int repeat;
    private boolean repeatable;
    public KafkaMessageKey(){}
    public KafkaMessageKey(K k,boolean repetable){
        this.k=k;
        this.repeatable=repetable;
        this.datetime=KafkaClient.getCurrentDateTime();
    }
    @SuppressWarnings("unchecked")
    public K getK(){
        if(k==null){
            synchronized(this){
                if(k==null){
                    k=(K)KafkaClient.createId();
                }
            }
        }
        return k;
    }
    public void setK(K k){
        this.k=k;
    }
    public long getDatetime(){
        return datetime;
    }
    public void setDatetime(long datetime){
        this.datetime=datetime;
    }
    public boolean getRepeatable(){
        return repeatable;
    }
    public void setRepeatable(boolean repeatable){
        this.repeatable=repeatable;
    }
    public int getRepeat(){
        return repeat;
    }
    public void setRepeat(int repeat){
        this.repeat=repeat;
    }
    /**
     * 自增重试次数，返回自增后的值。如果repeatEnabled是false只返回原值，不自增
     * @return
     */
    public int incrRepeat(){
        if(repeatable){
            return ++repeat;
        }else{
            return repeat;
        }
    }
    /**
     * kafka在没有key的时候会随机把消息发到某个partition，为了达到这个效果，在k为null时会自动用消息发送方的mac/进程号、线程号、当前时间生成一个key。
     * K类型必须实现hashCode和equals
     */
    @Override
    public int hashCode(){
        return getK().hashCode();
    }
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o){
        K k=getK();
        return o instanceof KafkaMessageKey && 
                (((KafkaMessageKey)o).getK()==k || 
                 ((KafkaMessageKey)o).getK().equals(k));
    }
}
