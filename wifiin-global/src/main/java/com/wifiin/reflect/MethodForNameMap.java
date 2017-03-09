package com.wifiin.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Maps;
import com.wifiin.reflect.exception.WifiinReflectException;

public class MethodForNameMap{
    private static final Map<MethodMeta<?>,Method> METHOD_FOR_NAME_MAP=Maps.newConcurrentMap();
    public static <T> Method get(Class<T> clazz,String method,Class<?>... argTypes){
        return METHOD_FOR_NAME_MAP.computeIfAbsent(new MethodMeta<T>(clazz,method,argTypes),(k)->{
            try{
                return clazz.getMethod(method,argTypes);
            }catch(NoSuchMethodException | SecurityException e){
                throw new WifiinReflectException(e);
            }
        });
    }
    private static class MethodMeta<T>{
        public Class<T> clazz;
        public String method;
        public Class<?>[] argTypes;
        private int hash;
        public MethodMeta(Class<T> clazz,String method,Class<?>... argTypes){
            this.clazz=clazz;
            this.method=method;
            this.argTypes=argTypes;
        }
        public int hashCode(){
            if(hash==0){
                synchronized(this){
                    if(hash==0){
                        hash=new HashCodeBuilder().append(clazz).append(method).append(argTypes).toHashCode();
                        if(hash==0){
                            hash=-1;
                        }
                    }
                }
            }
            return hash;
        }
        @SuppressWarnings("unchecked")
        public boolean equals(Object o){
            if(o instanceof MethodMeta){
                MethodMeta<T> mm=(MethodMeta<T>)o;
                return mm.clazz.equals(clazz) && mm.method.equals(method) && (
                        (mm.argTypes==null && argTypes==null) || 
                        (mm.argTypes!=null && argTypes!=null && Arrays.equals(mm.argTypes,argTypes))
                        );
            }
            return false;
        }
    }
}
