package com.wifiin.mq.mqtt.message;

import java.io.Serializable;
import java.util.Arrays;

import com.wifiin.common.GlobalObject;
import com.wifiin.mq.mqtt.constant.DupFlag;
import com.wifiin.mq.mqtt.constant.MessageType;
import com.wifiin.mq.mqtt.constant.QosLevel;
import com.wifiin.mq.mqtt.constant.Retain;
import com.wifiin.mq.mqtt.exception.UnSupportedDeserializationMethodAccessException;
import com.wifiin.mq.mqtt.message.serder.SerDerHolder;
import com.wifiin.nio.OutputObject;
import com.wifiin.util.Help;
import com.wifiin.util.message.Input;
import com.wifiin.util.message.IntMessageCodec;
import com.wifiin.util.message.Output;
import com.wifiin.util.message.exception.TooLargeIntMessageException;

public abstract class MqttMessage<TO,I extends Input,O extends Output> implements MqttSerializable<TO,O>,OutputObject,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-5155189848230829269L;
    public class MqttHeader implements Serializable,MqttHeaderDeserializable<I>{
        /**
         * 
         */
        private static final long serialVersionUID=4626384171741646957L;
        private byte header;
        private int length;
        public MqttHeader(){}
        public MqttHeader(byte header){
            this.header=header;
        }
        public MqttHeader(byte header, int length){
            this(header);
          this.length=length;
        }
        public MqttHeader(MessageType messageType,DupFlag dup,QosLevel qos,Retain retain){
            this((byte)(messageType.value()|dup.value()|qos.value()|retain.value()));
        }
        public MessageType messageType(){
            return MessageType.valueOf(header);
        }
        public DupFlag dup(){
            return DupFlag.valueOf(header);
        }
        public QosLevel qos(){
            return QosLevel.valueOf(header);
        }
        public Retain retain(){
            return Retain.valueOf(header);
        }
        public int length(){
            return length;
        }
        public void length(int length){
            this.length=length;
        }
        /**
         * <pre>
         * multiplier = 1 
         *  value = 0 
         *  do 
         *    digit = 'next digit from stream'
         *    value += (digit AND 127) * multiplier 
         *    multiplier *= 128
         *  while ((digit AND 128) != 0)
         *  </pre>
         * @param buf
         */
        public void parseLength(I buf){
            long l=IntMessageCodec.decode(buf,4);
            if(l>Integer.MAX_VALUE){
                throw new TooLargeIntMessageException(l,4);
            }
            this.length=(int)l;
        }
        public O serialize(){
            return serialize(wrap());
        }
        /**
         * <pre>
         * do
         *    digit = X MOD 128
         *    X = X DIV 128
         *    // if there are more digits to encode, set the top bit of this digit
         *    if ( X > 0 )
         *      digit = digit OR 0x80
         *    endif
         *    'output' digit
         * while ( X> 0 )
         * </pre>
         * */
        public O serialize(O buf){
            buf.markWriterIndex();
            buf.writeByte(header);
            IntMessageCodec.encode(this.length,buf,4);
            return buf;
        }
    }
    public class MqttBody implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID=-1758423646766749547L;
        private String topic;
        private byte[] tab;
        private byte[] key; 
        private byte[] payload;
        private transient byte[] body;
        public MqttBody(){}
        public MqttBody(String topic){
            this.topic=topic;
        }
        public <T extends Serializable,K extends Serializable,P extends Serializable> MqttBody(String topic,T tab,K key){
            this.topic=topic;
            this.tab=GlobalObject.getFSTConfiguration().asByteArray(tab);
            this.key=GlobalObject.getFSTConfiguration().asByteArray(key);
        }
        public <KT extends Serializable,P extends Serializable> MqttBody(String topic,KT keyOrTab,boolean key){
            this.topic=topic;
            if(key){
                this.key=GlobalObject.getFSTConfiguration().asByteArray(keyOrTab);
            }else{
                this.tab=GlobalObject.getFSTConfiguration().asByteArray(keyOrTab);
            }
        }
        public <T extends Serializable,K extends Serializable,P extends Serializable> MqttBody(String topic,T tab,K key,P payload){
            this.topic=topic;
            this.tab=GlobalObject.getFSTConfiguration().asByteArray(tab);
            this.key=GlobalObject.getFSTConfiguration().asByteArray(key);
            this.payload=GlobalObject.getFSTConfiguration().asByteArray(payload);
        }
        public <KT extends Serializable,P extends Serializable> MqttBody(String topic,KT keyOrTab,P payload,boolean key){
            this.topic=topic;
            if(key){
                this.key=GlobalObject.getFSTConfiguration().asByteArray(keyOrTab);
            }else{
                this.tab=GlobalObject.getFSTConfiguration().asByteArray(keyOrTab);
            }
            this.payload=GlobalObject.getFSTConfiguration().asByteArray(payload);
        }
        public <P extends Serializable> MqttBody(String topic,P payload){
            this.payload=GlobalObject.getFSTConfiguration().asByteArray(payload);
        }
        
        public String topic(){
            return topic;
        }
        
        public byte[] tabBytes(){
            return tab;
        }
        /**
         * 反序列化key的字节数组为对象，如果字节数组不包含类信息，请调用key(Class)
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T extends Serializable> T tab(){
            return (T)SerDerHolder.holder().serder().deserialize(tab);
        }
        /**
         * 反序列化key的字节数组为对象，如果字节数组包含类信息，请调用key()
         * @param kcls
         * @return
         */
        public <T extends Serializable> T tab(Class<T> tcls){
            return SerDerHolder.holder().serder().deserialize(tab,tcls);
        }
        
        public byte[] keyBytes(){
            return key;
        }
        /**
         * 反序列化key的字节数组为对象，如果字节数组不包含类信息，请调用key(Class)
         * @return
         */
        @SuppressWarnings("unchecked")
        public <K extends Serializable> K key(){
            return (K)SerDerHolder.holder().serder().deserialize(key);
        }
        /**
         * 反序列化key的字节数组为对象，如果字节数组包含类信息，请调用key()
         * @param kcls
         * @return
         */
        public <K extends Serializable> K key(Class<K> kcls){
            return SerDerHolder.holder().serder().deserialize(key,kcls);
        }
        public byte[] payloadBytes(){
            return payload;
        }
        @SuppressWarnings("unchecked")
        public <P extends Serializable> P payload(){
            return (P)SerDerHolder.holder().serder().deserialize(payload);
        }
        public <P extends Serializable> P payload(Class<P> pcls){
            return (P)SerDerHolder.holder().serder().deserialize(payload,pcls);
        }
        public byte[] serialize(){
            if(Help.isEmpty(body)){
                return body=SerDerHolder.holder().serder().serialize(this);
            }
            return body;
        }
        @Override
        public int hashCode(){
            if(Help.isNotEmpty(tab)){
                return Arrays.hashCode(tab);
            }
            if(Help.isNotEmpty(key)){
                return Arrays.hashCode(key);
            }
            return topic.hashCode();
        }
    }
    
    private MqttHeader header;
    private MqttBody body;
    public MqttMessage(){
        header=new MqttHeader();
    }
    public MqttMessage(byte header){
        this.header=new MqttHeader(header);
    }
    public MqttMessage<TO,I,O> messageType(MessageType messageType){
        header.header=messageType.format(header.header);
        return this;
    }
    public MqttMessage<TO,I,O> dup(DupFlag dup){
        header.header=dup.format(header.header);
        return this;
    }
    public MqttMessage<TO,I,O> qos(QosLevel qos){
        header.header=qos.format(header.header);
        return this;
    }
    public MqttMessage<TO,I,O> retain(Retain retain){
        header.header=retain.format(header.header);
        return this;
    }
    public MqttMessage<TO,I,O> length(int length){
        header.length=length;
        return this;
    }
    public MqttMessage<TO,I,O> body(String topic){
        body=new MqttBody(topic);
        body.topic=topic;
        return this;
    }
    public <K extends Serializable,P extends Serializable> MqttMessage<TO,I,O> body(String topic,K k, P payload){
        body=new MqttBody(topic,k,payload);
        return this;
    }
    public <P extends Serializable> MqttMessage<TO,I,O> body(String topic,P payload){
        body=new MqttBody(topic,payload);
        return this;
    }
    @SuppressWarnings("unchecked")
    public MqttBody body(byte[] content){
        try{
            body=(MqttBody)SerDerHolder.holder().serder().deserialize(content);
        }catch(UnSupportedDeserializationMethodAccessException e){
            body=SerDerHolder.holder().serder().deserialize(content,MqttBody.class);
        }
        body.body=content;
        return body;
    }
    public MqttHeader header(){
        return this.header;
    }
    public MqttBody body(){
        return body;
    }
    public O serialize(){
        return serialize(wrap());
    }
    public O serialize(O buf){
        byte[] payload=null;
        if(body==null){
            header.length=0;
        }else{
            payload=body.serialize();
            header.length=payload.length;
        }
        if(buf==null){
            buf=header.serialize();
        }else{
            buf=header.serialize(buf);
        }
        if(payload!=null){
            buf.writeBytes(payload);
        }
        return buf;
    }
    public static void main(String[] args){
        System.out.println(new java.util.Date(0x1_ff_ff_ff_ff_ffL));//31 millis
//        long id=(0x3_ff_ff_ff_ff_ffL<<13) | 
        System.out.println(0xf_ff);//12 sequence
        System.out.println(0x3_ff);//10 machine
        //messageId:31bit_millis 12bitsequence 16bitpid mac   
        //server client各自生成messageId，发布后，server给client返回server的messageId
        //相同tab的消息保证绝对有序，没有tab的消息不保证有序
        //tab决定接收消息的broker
        //tab决定同一consumer组内接收消息的consumer
        //server_messageId tab key topic -> data
        //topic server_messageId -> tab
        //topic tab server_messageId -> 1
        //topic key tab -> server_messageId //topic key保证惟一性或topic key tab保证惟一性
        //consumer_ip:port topic -> server_messageId  //保存每一个消费者的offset server_messageId
        //server_messageId -> {"ip1:port":CONSUMER_SENT,"ip2:port":CONSUMER_COMP,...} //已完成的消息，
    }
}
