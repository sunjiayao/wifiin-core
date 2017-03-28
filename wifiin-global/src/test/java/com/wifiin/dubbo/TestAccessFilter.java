package com.wifiin.dubbo;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestAccessFilter{
    @Test
    public void testAccessLogFilter(){
        ApplicationContext appContext=new ClassPathXmlApplicationContext("classpath:dubbo-test-filter.xml");
        ApplicationContextHolder.setApplicationContext(appContext);
        TestAccessLogA a=(TestAccessLogA)appContext.getBean("testAccessLogAClient");
        System.out.println(a.echo("hhhhh"));
    }
}
