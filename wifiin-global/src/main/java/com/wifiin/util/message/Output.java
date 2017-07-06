package com.wifiin.util.message;

public interface Output{
    public int writerIndex();
    public void writerIndex(int index);
    public void setByte(int index,int b);
    public void markWriterIndex();
    public void writeByte(int value);
    public void writeBytes(byte[] buf);
    public void resetWriterIndex();
}
