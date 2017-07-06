package com.wifiin.util.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ArrayByteBufOutput implements Output{
    private ByteBuf buf=Unpooled.buffer();
    public ByteBuf byteBuf(){
        return buf;
    }
    public byte[] byteArray(){
        byte[] bytes=new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
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