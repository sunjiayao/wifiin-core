package com.wifiin.cache.aop;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.cache.aop.exception.CacheAOPException;
import com.wifiin.common.GlobalObject;

public enum CachedDataFormat{
    JSON {
        @Override
        public String format(Object src){
            try{
                return src==null?null:GlobalObject.getJsonMapper().writeValueAsString(src);
            }catch(JsonProcessingException e){
                throw new CacheAOPException(e);
            }
        }

        @Override
        public <T> T parse(String data,Class<T> t){
            try{
                return GlobalObject.getJsonMapper().readValue(data,t);
            }catch(IOException e){
                throw new CacheAOPException(e);
            }
        }
    },
    PLAIN_TEXT {
        @Override
        public String format(Object src){
            return src==null?null:src.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T parse(String data,Class<T> t){
            if(data==null){
                return null;
            }else if(t.isAssignableFrom(String.class)){
                return (T)data;
            }else if(t.isAssignableFrom(Long.class) || t.isAssignableFrom(long.class)){
                return (T)Long.valueOf(data);
            }else if (t.isAssignableFrom(Integer.class) || t.isAssignableFrom(int.class)){
                return (T)Integer.valueOf(data);
            }else if(t.isAssignableFrom(BigDecimal.class)){
                return (T)new BigDecimal(data);
            }else if(t.isAssignableFrom(BigInteger.class)){
                return (T)new BigInteger(data);
            }else if(t.isAssignableFrom(Boolean.class) || t.isAssignableFrom(boolean.class)){
                return (T)Boolean.valueOf(data);
            }else if(t.isAssignableFrom(Double.class) || t.isAssignableFrom(double.class)){
                return (T)Double.valueOf(data);
            }else if(t.isAssignableFrom(Float.class) || t.isAssignableFrom(float.class)){
                return (T)Float.valueOf(data);
            }else{
                return (T)data;
            }
        }
    };
    public abstract String format(Object src);
    public abstract <T> T parse(String data,Class<T> t);
}
