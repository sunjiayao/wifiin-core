package com.wifiin.dubbo.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.wifiin.dubbo.ApplicationContextHolder;
import com.wifiin.dubbo.TestAccessLogA;
import com.wifiin.dubbo.TestAccessLogB;

@Component("testAccessLogA")
public class TestAccessLogAImpl implements TestAccessLogA{
//    @Resource(name="testAccessLogBClient")
//    private TestAccessLogB testAccessLogB;
    @Override
    public String echo(String arg){
//        return "!"+arg+"!";
        return "!"+ApplicationContextHolder.getApplicationContext().getBean("testAccessLogBClient",TestAccessLogB.class).echo(arg)+"!";
//        return "!"+testAccessLogB.echo(arg)+"!";
    }
    
}
