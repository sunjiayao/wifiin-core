package com.wifiin.kv.netty;

import com.wifiin.util.message.Input;

import io.netty.buffer.ByteBuf;

public class ByteBufInput implements Input{
    private ByteBuf buf;
    public ByteBufInput(ByteBuf buf){
        this.buf=buf;
    }
    public short readUnsignedByte(){
        return buf.readUnsignedByte();
    }
    public void read(byte[] bytes,int offset,int length){
        buf.readBytes(bytes,offset,length);
    }
    public byte[] read(int length){
        byte[] bytes=new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }
    public void read(byte[] buf,int length){
        read(buf,0,length);
    }
}
