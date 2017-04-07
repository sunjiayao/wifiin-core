package com.wifiin.multilanguage;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestMultiLangAspect{
    @Test
    public void testMultiLang(){
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath:/com/wifiin/multilanguage/conf/multilang.xml");
        MultiLangService service=context.getBean(MultiLangService.class);
        System.out.println(service.execute("en-US"));
        System.out.println(service.execute("","en-US"));
        System.out.println(service.execute(new TestMultiLangModel(),"en-US"));
        System.out.println(service.execute(new TestMultiLangModelWithLang()));
    }
}
