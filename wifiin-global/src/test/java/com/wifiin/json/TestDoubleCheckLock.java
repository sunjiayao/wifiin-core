package com.wifiin.json;

import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

public class TestDoubleCheckLock{
    private volatile static Map map;
    public static Map get(){
        if(map==null){
            synchronized(TestDoubleCheckLock.class){
                if(map==null){
                    Map m=Maps.newHashMap();
                    m.put(1,1);
                    map=m;
                }
            }
        }
        return map;
    }
}
