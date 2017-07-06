package com.wifiin.util.message;

import io.netty.buffer.ByteBuf;

public class ArrayByteBufInput implements Input{
    private ByteBuf buf;
    public ArrayByteBufInput(ByteBuf buf){
        this.buf=buf;
    }
    @Override
    public short readUnsignedByte(){
        return buf.readUnsignedByte();
    }
    
}
