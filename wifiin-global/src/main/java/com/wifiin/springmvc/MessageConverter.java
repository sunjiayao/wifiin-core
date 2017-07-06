package com.wifiin.springmvc;

public interface MessageConverter{
    public byte[] inputConvert(byte[] buf);
    public byte[] outputConvert(byte[] buf);
}
