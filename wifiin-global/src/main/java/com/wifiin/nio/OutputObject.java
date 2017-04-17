package com.wifiin.nio;

/**
 * 所有写出的对象都要实现这个接口
 * @author Running
 *
 */
public interface OutputObject{
    public static final OutputObject ACCOMPLISHED=new OutputObject(){};
    public static final OutputObject CLOSE_CHANNEL=new OutputObject(){};
}
