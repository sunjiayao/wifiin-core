package com.wifiin.dubbo.dynamic;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wifiin.rpc.dubbo.DynamicDubboConsumerMaker;

public class TestDynamic{
    @Test
    public void testDynamicClient(){
        ApplicationContext ctx=new ClassPathXmlApplicationContext("classpath:/com/wifiin/dubbo/dynamic/dubbo-test-dynamic.xml ");
        String result=DynamicDubboConsumerMaker.getInstance().get("helloworld",TestDynamicRPC.class,"testDynamic","1.0",false).helloworld();
        System.out.println("AAAAAAAAAAAAAAAA"+result);
        Assert.assertEquals("helloworld",result);
    }
}
