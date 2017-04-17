package com.wifiin.rpc.dubbo;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.google.common.collect.Maps;
import com.wifiin.util.Help;

public class DynamicDubboConsumerMaker implements ApplicationContextAware{
    private final Map<String,DynamicDubboConsumerWrapper<?>> DYNAMIC_DUBBO_CONSUMER_MAP=Maps.newConcurrentMap();
    private final class DynamicDubboConsumerWrapper<T>{
        private T consumer;
        private ReferenceBean<T> reference;
        private DynamicDubboConsumerWrapper(){
            init();
        }
        private void init(){
            reference=new ReferenceBean<>();
            reference.setScope("singleton");
            reference.setApplicationContext(appctx);
        }
        public DynamicDubboConsumerWrapper<T> interfaceClass(Class<T> interfaceClass){
            reference.setInterface(interfaceClass);
            return this;
        }
        public DynamicDubboConsumerWrapper<T> version(String version){
            if(Help.isNotEmpty(version)){
                reference.setVersion(version);
            }
            return this;
        }
        public DynamicDubboConsumerWrapper<T> group(String group){
            if(Help.isNotEmpty(group)){
                reference.setGroup(group);
            }
            return this;
        }
        public DynamicDubboConsumerWrapper<T> async(boolean async){
            reference.setAsync(async);
            return this;
        }
        public boolean initialized(){
            boolean init=consumer!=null;
            return init;
        }
        public T consumer(){
            if(consumer==null){
                synchronized(this){
                    if(consumer==null){
                        return consumer=reference.get();
                    }
                }
            }
            return consumer;
        }
    }
    private volatile static DynamicDubboConsumerMaker maker;
    private ApplicationContext appctx;
    private DynamicDubboConsumerMaker(){}
    /**
     * <pre>
     * <bean id="dynamicDubboConsumerMaker" class="{@code com.wifiin.rpc.dubbo.DynamicDubboConsumerMaker}" factory-method="getInstance"/>
     * </pre>
     * @return
     */
    public static DynamicDubboConsumerMaker getInstance(){
        if(maker==null){
            synchronized(DynamicDubboConsumerMaker.class){
                if(maker==null){
                    maker=new DynamicDubboConsumerMaker();
                }
            }
        }
        return maker;
    }
    @SuppressWarnings({"unchecked"})
    public <T> T get(String name,Class<T> interfaceClass,String group,String version,boolean async){
        return (T)DYNAMIC_DUBBO_CONSUMER_MAP.computeIfAbsent(name,(n)->{
            return new DynamicDubboConsumerWrapper<T>().interfaceClass(interfaceClass).group(group).version(version).async(async);
        }).consumer();
    }
    public <T> T get(String name,Class<T> interfaceClass,String group){
        return get(name,interfaceClass,group,null,false);
    }
    public <T> T get(Class<T> interfaceClass,String group){
        return get(group+"=>"+interfaceClass.getName(),interfaceClass,group);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext appctx) throws BeansException{
        this.appctx=appctx;
    }
}
