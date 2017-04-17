package com.wifiin.netty;

import java.io.Serializable;

import com.wifiin.nio.OutputObject;

public class TestOutputObject implements OutputObject,Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=2394496657984372109L;
    private long data;
    public TestOutputObject(){}
    public TestOutputObject(long data){
        this.data=data;
    }
    public long getData(){
        return data;
    }
    public void setData(long data){
        this.data=data;
    }
    
}
