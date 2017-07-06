package com.wifiin.util.message;

public class ByteArrayInput implements Input{
    private static final int MASK=0xff;
    private int index;
    private byte[] buf;
    public ByteArrayInput(byte[] buf){
        this.buf=buf;
    }
    @Override
    public short readUnsignedByte(){
        return (short)(buf[index++] & MASK);
    }
    
}
