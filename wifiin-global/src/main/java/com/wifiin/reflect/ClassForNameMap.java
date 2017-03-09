package com.wifiin.reflect;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.reflect.exception.WifiinReflectException;

public class ClassForNameMap{
    private static final Map<String,Class<?>> CLASS_NAME=Maps.newConcurrentMap();
    @SuppressWarnings("unchecked")
    public static <E> Class<E> get(String className){
        return (Class<E>)CLASS_NAME.computeIfAbsent(className,(c)->{
            try{
                return Class.forName(c);
            }catch(ClassNotFoundException e){
                throw new WifiinReflectException(e);
            }
        });
    }
}
