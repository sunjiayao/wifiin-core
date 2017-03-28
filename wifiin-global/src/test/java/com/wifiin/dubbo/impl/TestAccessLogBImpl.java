package com.wifiin.dubbo.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.wifiin.dubbo.ApplicationContextHolder;
import com.wifiin.dubbo.TestAccessLogB;
import com.wifiin.dubbo.TestAccessLogC;

@Component("testAccessLogB")
public class TestAccessLogBImpl implements TestAccessLogB{
//    @Resource(name="testAccessLogCClient")
//    private TestAccessLogC testAccessLogC;
    @Override
    public String echo(String arg){
//        return "@"+arg+"@";
        return "@"+ApplicationContextHolder.getApplicationContext().getBean("testAccessLogCClient",TestAccessLogC.class).echo(arg)+"@";
    }
    
}
