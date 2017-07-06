package com.wifiin.spring;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.wifiin.spring.exception.TooManyApplicationContextHolderException;

public class ApplicationContextHolder implements ApplicationContextAware{
    private static ApplicationContextHolder holder;
    private ApplicationContext appctx;
    public ApplicationContextHolder(){
        if(holder!=null && holder.appctx!=null){
            throw TooManyApplicationContextHolderException.instance; 
        }
        holder=this;
    }
    
    public static ApplicationContextHolder getInstance(){
        return holder;
    }
    @Override
    public void setApplicationContext(ApplicationContext appctx) throws BeansException{
        this.appctx=appctx;
    }
    public <T> T getBean(Class<T> c){
        return appctx.getBean(c);
    }
    public <T> T getBean(String name){
        return (T)appctx.getBean(name);
    }
    public <T> T getBean(String name,Class<T> requiredType){
        return appctx.getBean(name,requiredType);
    }
    public <T> Map<String,T> getBeans(Class<T> c){
        return appctx.getBeansOfType(c);
    }
    public <T> Map<String,T> getBeans(Class<T> c,boolean includeNonSingletons,boolean allowEagerInit){
        return appctx.getBeansOfType(c,includeNonSingletons,allowEagerInit);
    }
    public <T> String[] getBeanNames(Class<T> c){
        return appctx.getBeanNamesForType(c);
    }
    public <T> String[] getBeanNames(Class<T> c,boolean includeNonSingletons,boolean allowEagerInit){
        return appctx.getBeanNamesForType(c,includeNonSingletons,allowEagerInit);
    }
    
    public <T> T getBeanWithAnnotation(Class<? extends Annotation> c){
        return (T)appctx.getBeansWithAnnotation(c);
    }
    public Map<String,Object> getBeansWithAnnotation(Class<? extends Annotation> c){
        return appctx.getBeansWithAnnotation(c);
    }
    public String[] getBeanNamesWithAnnotation(Class<? extends Annotation> c){
        return appctx.getBeanNamesForAnnotation(c);
    }
    public ApplicationContext getApplicationContext(){
        return appctx;
    }
}
