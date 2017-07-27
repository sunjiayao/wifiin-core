package com.wifiin.util.message;

public interface Input{
    public short readUnsignedByte();
    public default void read(byte[] buf,int offset,int length){
        
    }
    public default byte[] read(int length){
        return null;
    }
    public default void read(byte[] buf,int length){
        read(buf,0,length);
    }
}
