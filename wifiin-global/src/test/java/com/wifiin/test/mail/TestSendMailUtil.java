package com.wifiin.test.mail;

import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.wifiin.spring.mail.SendMailUtil;

public class TestSendMailUtil{
    @Test
    public void testSendMailUtil(){
        ApplicationContext appctx=new ClassPathXmlApplicationContext("classpath:applicationContext-mail.xml");
        SendMailUtil mail=appctx.getBean(SendMailUtil.class);
        mail.setMailto("jingrun.wu@wifiin.com");
        mail.setContentTemplate("test ${hello} test");
        Map m=Maps.newHashMap();
        m.put("hello","helloworld");
        mail.setContent(m);
        mail.setSubject("test SendMailUtil");
        mail.send();
    }
}
