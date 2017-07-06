package com.wifiin.mq.mqtt.netty.decoder;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

@SuppressWarnings("rawtypes")
public class MqttBodyDecoder implements Decoder<MqttMessage>{
    public static final MqttBodyDecoder instance=new MqttBodyDecoder();
    @Override
    public MqttMessage decode(MqttMessage t,ByteBuf buf){
        byte[] dst=new byte[t.header().length()];
        buf.readBytes(dst,0,dst.length);
        t.body(dst);
        return t;
    }
    
}
