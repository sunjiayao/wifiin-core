package com.wifiin.kv.buf;

import com.wifiin.util.message.Input;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ThreadLocalByteBufIntput{
private static final ThreadLocal<ByteBufInput> input=new ThreadLocal<>();
    
    public static ByteBufInput input(byte[] buf){
        return input(Unpooled.wrappedBuffer(buf));
    }
    public static ByteBufInput input(ByteBuf buf){
        ByteBufInput in=input.get();
        if(in==null){
            in=new ByteBufInput(buf);
            input.set(in);
        }else{
            in.setBuf(buf);
        }
        return in;
    }
    
    public static class ByteBufInput implements Input{
        private ByteBuf buf;
        public ByteBufInput(ByteBuf buf){
            setBuf(buf);
        }
        public void setBuf(ByteBuf buf){
            this.buf=buf;
        }
        @Override
        public short readUnsignedByte(){
            return buf.readUnsignedByte();
        }
    }
}
