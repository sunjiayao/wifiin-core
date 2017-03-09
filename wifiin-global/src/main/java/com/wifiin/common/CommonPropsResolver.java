package com.wifiin.common;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import com.wifiin.springboot.ApplicationContextHolder;
import com.wifiin.util.Help;
/**
 * 通用properties属性获取类。
 * 首先从System.getProperty(...)获取，获取不到再从System.getenv(...)获取，还没有就从spring placeholder指定的properties文件获取 
 * @author Running
 *
 */
@Component
public class CommonPropsResolver implements EmbeddedValueResolverAware{
    private static CommonPropsResolver instance;
    private StringValueResolver resolver;
    /**
     * 只能被spring调用。内部方法。
     * @param resolver
     * @throws IllegalAccessError 如果参数是空或已经调用过一次，会抛出IllegalAccessError
     */
    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver){
        if(this.resolver!=null || resolver==null){
            throw new IllegalAccessError("this method must not be invoked two times, and the param must not be null");
        }
        this.resolver=resolver;
    }
    /**
     * 得到本类实例
     * @return
     */
    public static CommonPropsResolver getInstance(){
        if(instance==null){
            synchronized(CommonPropsResolver.class){
                if(instance==null){
                    instance=ApplicationContextHolder.getApplicationContext().getBean(CommonPropsResolver.class);
                }
            }
        }
        return instance;
    }
    /**
     * 得到指定key对应的值，获取顺序是System.getProperty(...)没有就从环境变量获取，还没有就从spring placeholder指定的属性文件获取
     * @param key
     * @return
     */
    public String getPropValue(String key){
        return (String)System.getProperties().computeIfAbsent(key,(k)->{
            return Help.convert(System.getenv(key),resolver.resolveStringValue("${"+key+"}"));
        });
    }
    /**
     * 得到指定KEY对应的值，并把值转化成int类型返回
     * @param key
     * @param defaultVal 如果没有得到相应的值，就使用defaultVal
     * @return
     */
    public int getIntConstant(String key, int defaultVal){
        String val=getPropValue(key);
        if(Help.isEmpty(val)){
            return defaultVal;
        }else{
            return Integer.parseInt(val);
        }
    }
    /**
     * 得到指定KEY对应的值，并把值转成long型并返回
     * @param key
     * @param defaultVal 如果没有得到相应的值，就使用defaultVal
     * @return
     */
    public long getLongConstant(String key, long defaultVal){
        String val=getPropValue(key);
        if(Help.isEmpty(val)){
            return defaultVal;
        }else{
            return Long.parseLong(val);
        }
    }
    /**
     * 得到指定KEY对应的值,并把值转成boolean类型并返回
     * @param key
     * @param defaultVal 如果没有得到相应的值，就使用defaultVal
     * @return
     */
    public boolean getBooleanConstant(String key, boolean defaultVal){
        String val=getPropValue(key);
        if(Help.isEmpty(val)){
            return defaultVal;
        }else{
            return Boolean.parseBoolean(val);
        }
    }
    /**
     * 得到指定KEY相应的值并返回
     * @param key
     * @param defaultVal 如果没有得到相应的值，就使用defaultVal
     * @return
     */
    public String getStringConstant(String key, String defaultVal){
        String val=getPropValue(key);
        if(Help.isEmpty(val)){
            return defaultVal;
        }else{
            return val;
        }
    }
    /**
     * 得到指定KEY相应的值并把值转成指定的枚举类型并返回
     * @param key
     * @param enumType
     * @param defaultVal 如果没有得到相应的值，就使用defaultVal
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum> E getEnumConstant(String key, Class enumType, Enum<?> defaultVal){
        String val=getPropValue(key);
        if(Help.isEmpty(val)){
            return (E)defaultVal;
        }else{
            return (E)Enum.valueOf(enumType, val);
        }
    }
}
