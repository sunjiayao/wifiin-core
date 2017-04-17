package com.wifiin.dubbo.dynamic;

import org.springframework.stereotype.Component;

@Component("testDynamicRPC")
public class TestDynamicRPCImpl implements TestDynamicRPC{
    @Override
    public String helloworld(){
        return "helloworld";
    }
}
