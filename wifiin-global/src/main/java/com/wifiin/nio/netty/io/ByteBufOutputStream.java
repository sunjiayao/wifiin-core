package com.wifiin.nio.netty.io;

import java.io.IOException;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;

public class ByteBufOutputStream extends OutputStream{
    public ByteBufOutputStream(ByteBuf buf){
        this.buf=buf;
    }
    private ByteBuf buf;
    
    @Override
    public void write(int b) throws IOException{
        buf.writeByte(b);
    }
    @Override
    public void write(byte[] buf){
        this.buf.writeBytes(buf);
    }
    @Override
    public void write(byte[] buf,int offset,int len){
        this.buf.writeBytes(buf,offset,len);
    }
}
