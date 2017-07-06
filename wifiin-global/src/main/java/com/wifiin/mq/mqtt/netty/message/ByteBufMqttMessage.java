package com.wifiin.mq.mqtt.netty.message;

import com.wifiin.mq.mqtt.message.MqttMessage;
import com.wifiin.nio.OutputObject;
import com.wifiin.util.message.Input;
import com.wifiin.util.message.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufMqttMessage extends MqttMessage<ByteBuf,Input,Output> implements OutputObject{

    /**
     * 
     */
    private static final long serialVersionUID=-3785428651242428059L;
    public ByteBufMqttMessage(){
        super();
    }
    public ByteBufMqttMessage(byte header){
        super(header);
    }
    
    @Override
    public Output wrap(ByteBuf buf){
        return new ByteBufOutput(buf);
    }

    @Override
    public Output wrap(){
        return new ByteBufOutput(PooledByteBufAllocator.DEFAULT.buffer());
    }
    private class ByteBufOutput implements Output{
        private ByteBuf buf;
        public ByteBufOutput(ByteBuf buf){
            this.buf=buf;
        }
        @Override
        public void markWriterIndex(){
            buf.markWriterIndex();
        }
        @Override
        public void writeByte(int value){
            buf.writeByte(value);
        }
        @Override
        public void writeBytes(byte[] buf){
            this.buf.writeBytes(buf);
        }
        @Override
        public void resetWriterIndex(){
            buf.resetWriterIndex();
        }
        @Override
        public int writerIndex(){
            return buf.writerIndex();
        }
        @Override
        public void writerIndex(int index){
            buf.writerIndex(index);
        }
        @Override
        public void setByte(int index,int b){
            buf.setByte(index,b);
        }
    }
}
