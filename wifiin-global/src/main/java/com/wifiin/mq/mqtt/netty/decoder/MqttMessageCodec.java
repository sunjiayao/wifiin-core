package com.wifiin.mq.mqtt.netty.decoder;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.jboss.netty.util.internal.ThreadLocalRandom;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.NettyContext;
import com.wifiin.nio.netty.NettyServerParams;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

@SuppressWarnings("rawtypes")
public class MqttMessageCodec extends AbstractCommonCodec<MqttMessage,OutputObject>{
    private static final String MQTT_MESSAGE_CODEC_THREAD="MqttMessageCodecThread-";
    private Executor[] executors;
    public MqttMessageCodec(){
        int l=((NettyServerParams)NettyContext.params()).poolCount();
        executors=new Executor[l];
        for(int i=0;i<l;i++){
            executors[i]=NettyContext.params().newBusinessExecutorService(MQTT_MESSAGE_CODEC_THREAD+i);
        }
    }
    @Override
    protected Decoder<MqttMessage> decoder(){
        return MqttHeaderDecoder.instance;
    }
    @Override
    protected void executor(MqttMessage mm,Consumer<MqttMessage> consumer){
        executors[ThreadLocalRandom.current().nextInt(executors.length)].execute(()->{
            consumer.accept(mm);
        });
    }

    @Override
    protected OutputObject execute(MqttMessage mm){
        return mm.header().messageType().execute(mm);
    }

    private void encode(MqttMessage o,ByteBuf buf){
        o.serialize(buf);
    }
    @Override
    protected void encode(OutputObject o,ByteBuf buf){
        if(o instanceof MqttMessage){
            encode((MqttMessage)o,buf);
        }
    }
    
}
