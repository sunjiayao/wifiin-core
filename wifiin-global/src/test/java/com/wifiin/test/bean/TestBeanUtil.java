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
        BeanUtil.set(t,"i",null,true);
        try{
            BeanUtil.set(t,"i",null,false);
        }catch(NullPointerException e){
            
        }
        Assert.assertEquals(1,((Integer)BeanUtil.get(t,"i",false)).intValue());
        Assert.assertEquals("ss",BeanUtil.get(t,"s",false));
        BeanUtil.set(t,"i",10,false);
        Assert.assertEquals(10,((Integer)BeanUtil.get(t,"i",false)).intValue());
        BeanUtil.set(t,"s","new",false);
        Assert.assertEquals("new",BeanUtil.get(t,"s",false));
        BeanUtil.set(t,"l",100L,false);
        Assert.assertEquals(100L,((Long)BeanUtil.get(t,"l",false)).longValue());
//        BeanUtil.populate(src,cls,populateEmpty,deep)
        int value=10000;
        TestBean tb=new TestBean(value);
        Assert.assertEquals(value,((Integer)BeanUtil.get(tb,"i",true)).intValue());
        Assert.assertEquals(value,((Integer)BeanUtil.get(tb,"wrappedI",true)).intValue());
        value=2418;
        BeanUtil.set(tb,"i",value,false);
        Assert.assertEquals(value,tb.getI());
        String s="helloworld";
        BeanUtil.set(tb,"s",s,false);
        Assert.assertEquals(s,tb.getS());
        BeanUtil.set(tb,"s",null,false);
        Assert.assertEquals(null,tb.getS());
        value=99999;
        BeanUtil.set(tb,"wrappedI",Integer.valueOf(value),false);
        Assert.assertEquals(value,tb.getWrappedI().intValue());
        value=33333;
        BeanUtil.set(tb,"wrappedI",value,false);
        Assert.assertEquals(value,tb.getWrappedI().intValue());
        BeanUtil.set(tb,"wrappedI",null,false);
        Assert.assertEquals(null,tb.getWrappedI());
        long lv=1L;
        BeanUtil.set(tb,"wrappedI",lv,false);
        Assert.assertEquals(lv,tb.getWrappedI().intValue());
        BeanUtil.set(tb,"i",lv,false);
        Assert.assertEquals(lv,tb.getI());
        
        lv=Long.MAX_VALUE;
        try{
            BeanUtil.set(tb,"wrappedI",lv,false);
        }catch(NumberFormatException e){
            Assert.assertEquals("For input string: \"9223372036854775807\"",e.getMessage());
        }
        try{
            BeanUtil.set(tb,"i",lv,false);
        }catch(NumberFormatException e){
            Assert.assertEquals("For input string: \"9223372036854775807\"",e.getMessage());
        }
    }
    public static class TestBean{
        private int i;
        private Integer wrappedI;
        private String s;
        public TestBean(int i){
            this.i=i;
            this.wrappedI=i;
        }
        public int getI(){
            return i;
        }
        public void setI(int i){
            this.i=i;
        }
        public Integer getWrappedI(){
            return wrappedI;
        }
        public void setWrappedI(Integer i){
            this.wrappedI=i;
        }
        public String getS(){
            return s;
        }
        public void setS(String s){
            this.s=s;
        }
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
    @Test
    public void test(){
        //{"orderId":"1K14879603WKLAGQ24","bytes":100,"incrementalBytes":10}
        Map m=Maps.newHashMap();
        m.put("orderId","1K14879603WKLAGQ24");
        m.put("bytes",100);
        m.put("incrementalBytes",10);
        BeanUtil.populateFromMap(m,IneFlowOrder.class,false,false);
    }
}
