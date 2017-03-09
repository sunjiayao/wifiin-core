package com.wifiin.dynamic.rpc;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wifiin.rpc.dubbo.DynamicDubboConsumerMaker;


public class DynamicRPCClientTest{
    @Test
    public void testDynamicRPC(){
        try(ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("classpath:dubbo-dynamic.xml")){
            DynamicRPC rpc=DynamicDubboConsumerMaker.consumer("com.wifiin.platform.usercenter.common.dynamic.rpc.test.DummyDynamicRPC",DynamicRPC.class);
            Assert.assertEquals(1000,rpc.execute("1000"));
        }
    }
}
