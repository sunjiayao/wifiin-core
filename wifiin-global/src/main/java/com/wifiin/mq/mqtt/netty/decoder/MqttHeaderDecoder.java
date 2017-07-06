package com.wifiin.mq.mqtt.netty.decoder;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.mq.mqtt.netty.message.ByteBufMqttMessage;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

@SuppressWarnings("rawtypes")
public class MqttHeaderDecoder implements Decoder<MqttMessage>{
    public static final MqttHeaderDecoder instance=new MqttHeaderDecoder();
    @Override
    public MqttMessage decode(MqttMessage t,ByteBuf buf){
        return new ByteBufMqttMessage(buf.readByte());
    }
    public Decoder<MqttMessage> next(){
        return MqttLengthDecoder.instance;
    }
}
