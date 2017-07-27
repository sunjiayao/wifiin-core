//package com.wifiin.test.configmanager;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.Assert;
//import org.junit.Test;
//
//import com.wifiin.config.ConfigManager;
//import com.wifiin.exception.ConfigException;
//
//public class ConfigManagerTest{
//    @Test
//    public void testMerge() throws Exception{
//        Properties props=new Properties();
//        props.put("a",1);
//        props.put("c",3);
//        ConfigManager.getInstance().setDataOrCreate("a.b.c",props);
//        props=new Properties();
//        props.put("a",2);
//        props.put("b",4);
//        ConfigManager.getInstance().setDataOrCreateToGlobal("a.b.c",props);
//        props=new Properties();
//        props.put("a",1);
//        props.put("b",4);
//        props.put("c",3);
//        System.out.println(props);
//        System.out.println(ConfigManager.getInstance().mergeProperties("a.b.c"));
//        Assert.assertEquals(props.toString(),ConfigManager.getInstance().mergeProperties("a.b.c").toString());
//        ConfigManager.getInstance().delete("a.b.c");
//        ConfigManager.getInstance().deleteFromGlobal("a.b.c");
//    }
//    @Test
//    public void testChangeValue(){
//        String key="a.b.c.change.value";
//        ConfigManager cm=ConfigManager.getInstance();
//        cm.setStringDataOrCreate(key,"test");
//        String v=cm.getString(key);
//        Assert.assertEquals("test",v);
//        for(int i=0;i<10;i++){
//            String newVal=RandomStringUtils.random(10,true,true);
//            cm.setStringDataOrCreate(key,newVal);
//            v=cm.getString(key);
//            Assert.assertEquals(newVal,v);
//        }
//        cm.delete(key);
//    }
//    @Test
//    public void testChangeProps() throws InterruptedException{
//        String key="a.b.c.change.props";
//        HashMap props=new HashMap();
//        props.put("a",1);
//        props.put("c",3);
//        ConfigManager.getInstance().setDataOrCreateInJson(key,props);
//        HashMap v=ConfigManager.getInstance().getDataFromJson(key,HashMap.class);
//        Assert.assertEquals(props,v);
//        props.put("a","4");
//        System.out.println(ConfigManager.getInstance().getString(key));
//        v=ConfigManager.getInstance().getDataFromJson(key,HashMap.class);
//        Assert.assertEquals(props,v);
//        ConfigManager.getInstance().delete(key);
//    }
//}
