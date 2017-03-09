package com.wifiin.kafka;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.util.Help;

public class SerdeFactory extends Serdes{
    private static final FstSerde FST_SERDE=new FstSerde();
    private static final AutoSerde AUTO_SERDE=new AutoSerde();
    public static AutoSerde Auto(){
        return AUTO_SERDE;
    }
    public static FstSerde Fst(){
        return FST_SERDE;
    }
    /**
     * 内置自动序列化器
     * @see SerdeAlgo
     * @author Running
     *
     */
    public static class AutoSerializer implements Serializer<Object>{
        private boolean isKey;
        public AutoSerializer(){}
        @Override
        public void configure(Map<String,?> configs,boolean isKey){
            this.isKey=isKey;
        }
        @Override
        public byte[] serialize(String topic,Object data){
            if(data==null){
                return KafkaClient.EMPTY_BYTES;
            }
            return Fst().serializer().serialize(topic,data);
        }
        @Override
        public void close(){
            // do nothing
        }
    }
    /**
     * 内置自动反序列化器
     * @see SerdeAlgo
     * @author Running
     *
     */
    public static class AutoDeserializer implements Deserializer<Object>{
        private boolean isKey;
        public AutoDeserializer(){}
        @Override
        public void configure(Map<String,?> configs,boolean isKey){
            this.isKey=isKey;
        }
        @Override
        public Object deserialize(String topic,byte[] data){
            if(Help.isEmpty(data)){
                return null;
            }
            return Fst().deserializer().deserialize(topic,data);
        }
        @Override
        public void close(){
            // do nothing
        }
    }
    /**
     * 默认采用jackson，如果在configs指定了序列化反序列化方式就使用指定的，使用指定方式或默认方式失败，就尝试其余方式。
     * 目前支持jackson、fst两种方式
     * @author Running
     */
    public static class AutoSerde extends Serdes.WrapperSerde<Object>{
        public AutoSerde(){
            this(new AutoSerializer(),new AutoDeserializer());
        }
        private AutoSerde(Serializer<Object> serializer,Deserializer<Object> deserializer){
            super(serializer,deserializer);
        }
    }
    public static class FstSerde extends Serdes.WrapperSerde<Object>{
        public FstSerde(){
            this(new Serializer<Object>(){
                @Override
                public void close(){
                    //do nothing
                }
                @Override
                public void configure(Map<String,?> arg0,boolean arg1){
                    //do nothing
                }
                @Override
                public byte[] serialize(String topic,Object data){
                    if(data==null){
                        return KafkaClient.EMPTY_BYTES;
                    }
                    return GlobalObject.getFSTConfiguration().asByteArray(data);
                }
            },new Deserializer<Object>(){
                private boolean isKey;
                @Override
                public void close(){
                    //do nothing
                }
                @Override
                public void configure(Map<String,?> config,boolean isKey){
                    this.isKey=isKey;
                }
                @Override
                public Object deserialize(String topic,byte[] data){
                    if(Help.isEmpty(data)){
                        return null;
                    }
                    return GlobalObject.getFSTConfiguration().asObject(data);
                }
            });
        }
        private FstSerde(Serializer<Object> serializer,Deserializer<Object> deserializer){
            super(serializer,deserializer);
        }
    }
    public static void main(String[] args) throws JsonProcessingException, UnsupportedEncodingException{
        //[3, 1, 116, 101, 115, 116, 46, 107, 101, -7]
        System.out.println(GlobalObject.getJsonMapper().writeValueAsString("test.key"));
        System.out.println(java.util.Arrays.toString(GlobalObject.getJsonMapper().writeValueAsBytes("test.key")));
        System.out.println(java.util.Arrays.toString("test.key".getBytes("utf8")));
        System.out.println(java.util.Arrays.toString("test.key".getBytes()));
    }
}
