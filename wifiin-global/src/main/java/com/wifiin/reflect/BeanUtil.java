package com.wifiin.reflect;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.reflect.exception.BeanPropertyPopulationException;
import com.wifiin.reflect.exception.GetterGenerationException;
import com.wifiin.reflect.exception.SetterGenerationException;
import com.wifiin.reflect.getset.GetSetUtil;
import com.wifiin.reflect.getset.Getter;
import com.wifiin.reflect.getset.Setter;
import com.wifiin.util.Help;
import com.wifiin.util.regex.RegexUtil;

/**
 * 获取指定对象指定属性的值
 * 将指定对象属性值填充到另一个对象
 * 将指定map的属性值填充到另一个对象
 * 
 * @author Running
 *
 */
public class BeanUtil{
    /**
     * 从指定对象获取指定属性的值
     * @param src
     * @param property
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <O,V> V get(O src,String property){
        Getter<O,V> getter=(Getter<O,V>)GetSetUtil.getGetters(src.getClass()).get(property);
        if(getter!=null){
            return getter.get(src);
        }else{
            throw new GetterGenerationException("there is no specified property getter method in src, or the property is not public");
        }
    }
    @SuppressWarnings("unchecked")
    public static <O,V> void set(O src,String property,V v){
        Setter<O,V> setter=(Setter<O,V>)GetSetUtil.getSetters(src.getClass()).get(property);
        if(setter!=null){
            setter.set(src,v);
        }else{
            throw new SetterGenerationException("there is no specified property setter method in src, or the property is not public");
        }
    }
    public static <O> O populate(Object src, Class<O> cls,boolean populateEmpty, boolean deep) {
        try{
            return populate(src,cls.newInstance(),populateEmpty,deep);
        }catch(InstantiationException | IllegalAccessException e){
            throw new BeanPropertyPopulationException(e);
        }
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <O> O populate(Object src,O dest,boolean populateEmpty,boolean deep){
        Class clazz=dest.getClass();
        Map<String,Setter<?,?>> setters=GetSetUtil.getSetters(clazz);
        for(Entry<String,Getter<?,?>> entry:GetSetUtil.getGetters(src.getClass()).entrySet()){
            Setter setter=setters.get(entry.getKey());
            if(setter!=null){
                Object value=entry.getValue();
                populate(value,dest,setter,populateEmpty,deep);
            }
        }
        return dest;
    }
    public static <O> O populate(Map<String,Object> src, Class<O> cls,boolean populateEmpty,boolean deep){
        try{
            return populate(src,cls.newInstance(),populateEmpty,deep);
        }catch(InstantiationException | IllegalAccessException e){
            throw new BeanPropertyPopulationException(e);
        }
    }
    
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <O> O populate(Map<String,Object> src,O dest,boolean populateEmpty,boolean deep){
        Class clazz=dest.getClass();
        Map<String,Setter<?,?>> setters=GetSetUtil.getSetters(clazz);
        for(Map.Entry<String,Object> entry:src.entrySet()){
            String property=entry.getKey();
            Setter setter=setters.get(property);
            if(setter!=null){
                populate(entry.getValue(),dest,setter,populateEmpty,deep);
            }
        }
        return dest;
    }
    public static <O> O populate(Map<String,Object> src, Class<O> cls, boolean populateEmpty, boolean deep, String... properties) throws InstantiationException, IllegalAccessException{
        return populate(src,cls.newInstance(),populateEmpty,deep,properties);
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <O> O populate(Map<String,Object> src,O dest,boolean populateEmpty,boolean deep,String... properties){
        Class clazz=dest.getClass();
        Map<String,Setter<?,?>> setters=GetSetUtil.getSetters(clazz);
        for(int i=0,l=properties.length;i<l;i++){
            try{
                String property=properties[i];
                Setter setter=setters.get(property);
                if(setter!=null){
                    Object value=src.get(property);
                    populate(value,dest,setter,populateEmpty,deep);
                }
            }catch(Exception e){}
        }
        return dest;
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private static void populate(Object value,Object dest,Setter setter,boolean populateEmpty,boolean deep){
        if(populateEmpty || Help.isNotEmpty(value)){
            Class propertyType=setter.propertyType();
            if(value instanceof String){
                if(!propertyType.isAssignableFrom(String.class)){
                    value=Help.parse(propertyType,Help.trim((String)value));
                }else if(propertyType.isAssignableFrom(StringBuilder.class) && deep){
                    value=new StringBuilder(value.toString());
                }
            }else if(propertyType.isAssignableFrom(String.class)){
                value=value.toString();
            }else if(propertyType.isAssignableFrom(StringBuilder.class) && deep){
                value=new StringBuilder(value.toString());
            }else if(!(propertyType.isPrimitive() || propertyType.equals(Number.class) || Number.class.isAssignableFrom(propertyType))){
                value=newValue(propertyType,value,populateEmpty,deep);
            }
            setter.set(dest,value);
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private static Object newValue(Class<?> propertyType,Object value,boolean populateEmpty,boolean deep){
        if(!deep){
            return value;
        }
        if(propertyType.equals(String.class)){
            return value==null?value:value.toString();
        }
        if(propertyType.equals(StringBuilder.class)){
            return value==null?value:new StringBuilder(value.toString());
        }
        boolean emptyValue=value==null || "".equals(value);
        if(propertyType.isPrimitive() && emptyValue){
            return propertyType.equals(boolean.class)?false:0;
        }
        boolean numberProperty=propertyType.equals(Number.class) || Number.class.isAssignableFrom(propertyType);
        if((numberProperty || propertyType.equals(boolean.class) || Boolean.class.equals(propertyType)) && emptyValue){
            return null;
        }
        if(propertyType.equals(int.class) || propertyType.equals(Integer.class)){
            if(value instanceof Number){
                return ((Number)value).intValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d+$")){
                    return Integer.valueOf(v);
                }else if(RegexUtil.isHex(v)){
                    return Integer.valueOf(v,16);
                }else if(RegexUtil.matches(v,"^[01]+$")){
                    return Integer.valueOf(v,2);
                }else if(RegexUtil.matches(v,"^[a-zA-Z0-9]+$")){
                    return Integer.valueOf(v,36);
                }else{
                    return propertyType.equals(int.class)?0:null;
                }
            }
        }
        if(propertyType.equals(long.class) || propertyType.equals(Long.class)){
            if(value instanceof Number){
                return ((Number)value).longValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d+$")){
                    return Long.valueOf(v);
                }else if(RegexUtil.isHex(v)){
                    return Long.valueOf(v,16);
                }else if(RegexUtil.matches(v,"^[01]+$")){
                    return Long.valueOf(v,2);
                }else if(RegexUtil.matches(v,"^[a-zA-Z0-9]+$")){
                    return Long.valueOf(v,36);
                }else{
                    return propertyType.equals(long.class)?0:null;
                }
            }
        }
        if(propertyType.equals(short.class) || propertyType.equals(Short.class)){
            if(value instanceof Number){
                return ((Number)value).shortValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d+$")){
                    return Short.valueOf(v);
                }else if(RegexUtil.isHex(v)){
                    return Short.valueOf(v,16);
                }else if(RegexUtil.matches(v,"^[01]+$")){
                    return Short.valueOf(v,2);
                }else if(RegexUtil.matches(v,"^[a-zA-Z0-9]+$")){
                    return Short.valueOf(v,36);
                }else{
                    return propertyType.equals(short.class)?0:null;
                }
            }
        }
        if(propertyType.equals(byte.class) || propertyType.equals(Byte.class)){
            if(value instanceof Number){
                return ((Number)value).byteValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d+$")){
                    return Byte.valueOf(v);
                }else if(RegexUtil.isHex(v)){
                    return Byte.valueOf(v,16);
                }else if(RegexUtil.matches(v,"^[01]+$")){
                    return Byte.valueOf(v,2);
                }else if(RegexUtil.matches(v,"^[a-zA-Z0-9]+$")){
                    return Byte.valueOf(v,36);
                }else{
                    return propertyType.equals(byte.class)?0:null;
                }
            }
        }
        if(propertyType.equals(char.class) || propertyType.equals(Character.class)){
            if(value instanceof Character){
                return ((Character)value);
            }else if(value instanceof Number){
                return (char)(((Number)value).intValue());
            }else{
                String v=value.toString();
                return v.charAt(0);
            }
        }
        if(propertyType.equals(float.class) || propertyType.equals(Float.class)){
            if(value instanceof Number){
                return ((Number)value).floatValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d*\\.?\\d*$")){
                    return Float.valueOf(v);
                }else{
                    return propertyType.equals(float.class)?0:null;
                }
            }
        }
        if(propertyType.equals(double.class) || propertyType.equals(Double.class)){
            if(value instanceof Number){
                return ((Number)value).doubleValue();
            }else{
                String v=value.toString();
                if(RegexUtil.matches(v,"^\\d*\\.?\\d*$")){
                    return Double.valueOf(v);
                }else{
                    return propertyType.equals(double.class)?0:null;
                }
            }
        }
        if(propertyType.equals(boolean.class) || propertyType.equals(Boolean.class)){
            if(value instanceof Boolean){
                return ((Boolean)value);
            }else if(value instanceof Number){
                return ((Number)value).intValue()==0?false:true;
            }else{
                String v=value.toString();
                if("true".equals(v) || "false".equals(v)){
                    return Boolean.valueOf(v);
                }else if(RegexUtil.matches(v,"^\\d+\\.?\\d*|\\d*\\.\\d+$")){
                    return new BigDecimal(v).longValue()==0?false:true;
                }else{
                    return propertyType.equals(boolean.class)?false:null;
                }
            }
        }
        if(propertyType.equals(BigInteger.class)){
            if(value instanceof BigInteger){
                return (BigInteger)value;
            }else if(value instanceof Number){
                return BigInteger.valueOf(((Number)value).longValue());
            }else{
                String v=value.toString();
                return new BigInteger(v);
            }
        }
        if(propertyType.equals(BigDecimal.class)){
            if(value instanceof BigDecimal){
                return (BigDecimal)value;
            }else if(value instanceof Number){
                return BigDecimal.valueOf(((Number)value).doubleValue());
            }else{
                String v=value.toString();
                return new BigDecimal(v);
            }
        }
        if(Calendar.class.isAssignableFrom(propertyType)){
            Calendar calendar=Calendar.getInstance();
            long millis=parseMillis(value);
            calendar.setTimeInMillis(millis);
            value=calendar;
        }else if(Date.class.isAssignableFrom(propertyType)){
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(((Calendar)value).getTimeInMillis());
            value=calendar.getTime();
        }else if(Instant.class.isAssignableFrom(propertyType)){
            value=Instant.ofEpochMilli(parseMillis(value));
        }else if(propertyType.isArray()){
            int len=Array.getLength(value);
            Object newValue=Array.newInstance(propertyType,len);
            for(int i=0;i<len;i++){
                Object v=Array.get(value,i);
                Array.set(newValue,i,newValue(v.getClass(),v,populateEmpty,deep));
            }
            value=newValue;
        }else if(Collection.class.isAssignableFrom(propertyType) && value instanceof Collection){
            try{
                Collection c=(Collection)value.getClass().newInstance();
                for(Object v:(Collection)value){
                    c.add(newValue(v.getClass(),v,populateEmpty,deep));
                }
                value=c;
            }catch(Exception e){
                throw new BeanPropertyPopulationException(e);
            }
        }else if(Map.class.isAssignableFrom(propertyType) && value instanceof Map){
            try{
                Map m=(Map)value.getClass().newInstance();
                for(Object o:((Map)value).entrySet()){
                    Map.Entry entry=(Map.Entry)o;
                    Object k=entry.getKey();
                    Object v=entry.getValue();
                    m.put(newValue(k==null?Object.class:k.getClass(),k,populateEmpty,deep),newValue(v==null?Object.class:v.getClass(),v,populateEmpty,deep));
                }
                value=m;
            }catch(Exception e){
                throw new BeanPropertyPopulationException(e);
            }
        }else{
            try{
                value=populate(value,propertyType.newInstance(),populateEmpty,deep);
            }catch(Exception e){
                throw new BeanPropertyPopulationException(e);
            }
        }
        return value;
    }
    
    private static long parseMillis(Object value){
        long l=0;
        if(value instanceof Calendar){
            l=((Calendar)value).getTimeInMillis();
        }else if(value instanceof Date){
            l=((Date)value).getTime();
        }else if(value instanceof Instant){
            return ((Instant)value).toEpochMilli();
        }else if(value instanceof Clock){
            return ((Clock)value).millis();
        }else if(value instanceof Number){
            l=((Number)value).longValue();
        }else{
            String s=value.toString();
            if(RegexUtil.matches(s,"^\\d+\\.?\\d*|\\d*\\.\\d+$")){
                l=new BigDecimal(s).longValue();
            }
        }
        return l;
    }
}
