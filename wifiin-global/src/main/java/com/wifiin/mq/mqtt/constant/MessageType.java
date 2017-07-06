package com.wifiin.mq.mqtt.constant;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.mq.mqtt.message.executors.MqttMessageExecutor;
import com.wifiin.mq.mqtt.netty.exception.IllegalHeaderException;
import com.wifiin.nio.OutputObject;

public enum MessageType{
    RESERVED_00(0x00) {
        @Override
        public int value(){
            throw new IllegalHeaderException("message type 0x00 is reserved");
        }

        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.RESERVED_0X00_EXECUTOR.execute(mm);
        }

        @Override
        public byte format(byte format){
            throw new IllegalHeaderException("message type 0x00 is reserved");
        }
    },
    CONNECT(0x10) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.CONNECT_EXECUTOR.execute(mm);
        }
    },
    CONNACK(0x20) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.CONN_ACK_EXECUTOR.execute(mm);
        }
    },
    PUBLISH(0x30) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PUBLISH_EXECUTOR.execute(mm);
        }
    },
    PUBACK(0x40) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PUB_ACK_EXECUTOR.execute(mm);
        }
    },
    PUBREC(0x50) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PUB_REC_EXECUTOR.execute(mm);
        }
    },
    PUBREL(0x60) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PUB_REL_EXECUTOR.execute(mm);
        }
    },
    PUBCOMP(0x70) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PUB_COMP_EXECUTOR.execute(mm);
        }
    },
    SUBSCRIBE(0x80) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.SUBSCRIBE_EXECUTOR.execute(mm);
        }
    },
    SUBACK(0x90) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.SUB_ACK_EXECUTOR.execute(mm);
        }
    },
    UNSUBSCRIBE(0xa0) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.UNSUBSCRIBE_EXECUTOR.execute(mm);
        }
    },
    UNSUBACK(0xb0) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.UNSUB_ACK_EXECUTOR.execute(mm);
        }
    },
    PINGREQ(0xc0) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PING_REQ_EXECUTOR.execute(mm);
        }
    },
    PINGRESP(0xd0) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.PING_RESP_EXECUTOR.execute(mm);
        }
    },
    DISCONNECT(0xe0) {
        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.DISCONNECT_EXECUTOR.execute(mm);
        }
    },
    RESERVED_F0(0xf0) {
        @Override
        public int value(){
            throw new IllegalHeaderException("message type 0xf0 is reserved");
        }

        @SuppressWarnings("rawtypes")
        @Override
        public OutputObject execute(MqttMessage mm){
            return MqttMessageExecutor.RESERVED_0XF0_EXECUTOR.execute(mm);
        }
        @Override
        public byte format(byte format){
            throw new IllegalHeaderException("message type 0xf0 is reserved");
        }
    };
    private static final Map<Integer,MessageType> VALUE_MAP;
    static{
        Builder<Integer,MessageType> builder=ImmutableMap.<Integer,MessageType>builder();
        for(MessageType t:MessageType.values()){
            builder.put(t.value,t);
        }
        VALUE_MAP=builder.build();
    }
    private MessageType(int value){
        this.value=value;
    }
    private int value;
    public int value(){
        return value;
    }
    @SuppressWarnings("rawtypes")
    public abstract OutputObject execute(MqttMessage mm);
    public byte format(byte value){
        return (byte)((0x0f&value)|this.value);
    }
    public static MessageType valueOf(int value){
        MessageType type=VALUE_MAP.get(value & RESERVED_F0.value);
        type.value();
        return type;
    }
}
