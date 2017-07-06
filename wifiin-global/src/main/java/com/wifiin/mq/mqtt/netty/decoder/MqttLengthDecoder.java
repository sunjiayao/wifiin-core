package com.wifiin.mq.mqtt.netty.decoder;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

@SuppressWarnings("rawtypes")
public class MqttLengthDecoder implements Decoder<MqttMessage>{
    public static final MqttLengthDecoder instance=new MqttLengthDecoder();
    @SuppressWarnings("unchecked")
    @Override
    public MqttMessage decode(MqttMessage t,ByteBuf buf){
        t.header().parseLength(()->{
            return buf.readUnsignedByte();
        });
        return t;
    }
    public Decoder<MqttMessage> next(){
        return MqttBodyDecoder.instance;
    }
}
