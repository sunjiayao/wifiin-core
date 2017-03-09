package com.wifiin.test.bean;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.reflect.BeanUtil;


public class TestBeanUtil{
    @Test
    public void testBeanUtil(){
        com.wifiin.test.bean.Test t=new com.wifiin.test.bean.Test();
        Assert.assertEquals(1,((Integer)BeanUtil.get(t,"i")).intValue());
        Assert.assertEquals("ss",BeanUtil.get(t,"s"));
        BeanUtil.set(t,"i",10);
        Assert.assertEquals(10,((Integer)BeanUtil.get(t,"i")).intValue());
        BeanUtil.set(t,"s","new");
        Assert.assertEquals("new",BeanUtil.get(t,"s"));
        BeanUtil.set(t,"l",100L);
        Assert.assertEquals(100L,((Long)BeanUtil.get(t,"l")).longValue());
//        BeanUtil.populate(src,cls,populateEmpty,deep)
    }
    @Test
    public void testPopulate() throws JsonProcessingException{
        Map m=Maps.newHashMap();
        m.put("s","ssssss");
        m.put("i","1123");
        m.put("l","1234241");
        m.put("b","true");
        System.out.println(GlobalObject.getJsonMapper().writeValueAsString(BeanUtil.populate(m,com.wifiin.test.bean.Test.class,false,false)));
    }
}
