package com.wifiin.dubbo.impl;

import org.springframework.stereotype.Component;

import com.wifiin.dubbo.TestAccessLogC;

@Component("testAccessLogC")
public class TestAccessLogCImpl implements TestAccessLogC{
    @Override
    public String echo(String arg){
        return "#"+arg+"#";
    }
    
}
