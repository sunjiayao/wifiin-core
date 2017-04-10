package com.wifiin.nio.netty;

/**
 * 所有写出的对象都要实现这个接口
 * @author Running
 *
 */
public interface OutputObject{
    public static final class AccomplishedOutputObject implements OutputObject{
        private AccomplishedOutputObject(){}
        private static final AccomplishedOutputObject ACCOMPLISHED=new AccomplishedOutputObject();
    }
    public static final AccomplishedOutputObject ACCOMPLISHED=AccomplishedOutputObject.ACCOMPLISHED;
}
