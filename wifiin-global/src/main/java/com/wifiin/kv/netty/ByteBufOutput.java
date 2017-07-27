package com.wifiin.kv.netty;

import com.wifiin.util.message.Output;

import io.netty.buffer.ByteBuf;

public class ByteBufOutput implements Output{
    private ByteBuf buf;
    public ByteBufOutput(ByteBuf buf){
        this.buf=buf;
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

    @Override
    public void markWriterIndex(){
        buf.markWriterIndex();
    }

    @Override
    public void writeByte(int value){
        buf.writeByte(value);
    }

    @Override
    public void writeBytes(byte[] bytes){
        buf.writeBytes(bytes);
    }

    @Override
    public void resetWriterIndex(){
        buf.resetWriterIndex();
    }
    
}
