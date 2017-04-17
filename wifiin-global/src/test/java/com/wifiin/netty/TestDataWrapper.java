package com.wifiin.netty;

import java.io.Serializable;

import com.wifiin.nio.OutputObject;

public class TestDataWrapper implements OutputObject,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=8970322635600009154L;
    public int len;
    public TestOutputObject data;
    public TestDataWrapper(int len){
        this.len=len;
    }
}
