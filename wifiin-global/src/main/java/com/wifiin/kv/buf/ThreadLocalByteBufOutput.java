package com.wifiin.kv.buf;

import com.wifiin.util.message.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ThreadLocalByteBufOutput{
    private static final ThreadLocal<ByteBufOutput> output=new ThreadLocal<>();
    
    public static ByteBufOutput output(){
        ByteBufOutput out=output.get();
        if(out==null){
            out=new ByteBufOutput();
            output.set(out);
        }else{
            out.resetWriterIndex();
        }
        out.markWriterIndex();
        return out;
    }
    
    public static class ByteBufOutput implements Output{
        private ByteBuf buf=Unpooled.buffer();
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
        public void writeBytes(byte[] buf){
            this.buf.writeBytes(buf);
        }

        @Override
        public void resetWriterIndex(){
            buf.resetWriterIndex();
        }
        
        public byte[] bytes(){
            byte[] bytes=new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        }
    }
}
