package com.wifiin.nio.netty.io;

import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;

public class ByteBufInputStream extends InputStream{
    private ByteBuf buf;
    public ByteBufInputStream(ByteBuf buf){
        this.buf=buf;
    }
    @Override
    public int read() throws IOException{
        return buf.readByte();
    }

    public int read(byte b[], int off, int len) throws IOException {
        int readable=buf.readableBytes();
        readable=readable>=len?len:readable;
        buf.readBytes(b,off,readable);
        return readable;
    }

    public long skip(long n) throws IOException {
        int skiped=buf.readableBytes()>=n?(int)n:buf.readableBytes();
        buf.skipBytes(skiped);
        return skiped;
    }

    public int available() throws IOException {
        return buf.readableBytes();
    }
    /**
     * 只能mark当前位置，不能mark指定位置 
     * @param readlimit 没有用
     */
    @Override
    public synchronized void mark(int readlimit) {
        buf.markReaderIndex();
    }

    @Override
    public synchronized void reset() throws IOException {
        buf.resetReaderIndex();
    }
    @Override
    public boolean markSupported() {
        return true;
    }
}
