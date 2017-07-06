package com.wifiin.rpc.dubbo;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.google.common.collect.Maps;
import com.wifiin.util.Help;

public class DynamicDubboConsumerMaker implements ApplicationContextAware{
    private final Map<String,DynamicDubboConsumerWrapper<?>> DYNAMIC_DUBBO_CONSUMER_MAP=Maps.newConcurrentMap();
    private final class DynamicDubboConsumerWrapper<T>{
        private ThreadLocal<T> consumerThreadLocal=new ThreadLocal<>();
        private volatile T consumer;
        private ReferenceBean<T> reference;
        private DynamicDubboConsumerWrapper(){
            init();
        }
        private void init(){
            reference=new ReferenceBean<>();
            reference.setScope("singleton");
            reference.setApplicationContext(getApplicationContext());
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
            T c=consumerThreadLocal.get();
            if(c==null){
                if(consumer==null){
                    synchronized(this){
                        if(consumer==null){
                            c=reference.get();
                            consumer=c;
                            consumerThreadLocal.set(c);
                            return c;
                        }else{
                            c=consumer;
                            consumerThreadLocal.set(c);
                            return c;
                        }
                    }
                }else{
                    c=consumer;
                    consumerThreadLocal.set(c);
                    return c;
                }
            }
            return c;
        }
    }
    private volatile static DynamicDubboConsumerMaker maker;
    private volatile ApplicationContext appctx;
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
            DynamicDubboConsumerWrapper<T> wrapper=new DynamicDubboConsumerWrapper<T>().interfaceClass(interfaceClass);
            if(group!=null){
                wrapper.group(group);
            }
            if(version!=null){
                wrapper.version(version);
            }
            return wrapper.async(async);
        }).consumer();
    }
    public <T> T get(String name,Class<T> interfaceClass,String group, String version){
        return get(name,interfaceClass,group,version,false);
    }
    public <T> T get(String name,Class<T> interfaceClass,String group){
        return get(name,interfaceClass,group,null,false);
    }
    public <T> T get(Class<T> interfaceClass,String group,String version,boolean async){
        return get(group+"=>"+interfaceClass.getName()+"-"+version,interfaceClass,version,group,async);
    }
    public <T> T get(Class<T> interfaceClass,String group,String version){
        return get(group+"=>"+interfaceClass.getName()+"-"+version,interfaceClass,version,group);
    }
    public <T> T get(Class<T> interfaceClass,String group){
        return get(group+"=>"+interfaceClass.getName(),interfaceClass,group);
    }
    public <T> T get(Class<T> interfaceClass){
        return get(interfaceClass.getName(),interfaceClass,null);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext appctx) throws BeansException{
        this.appctx=appctx;
    }
    private ApplicationContext getApplicationContext(){
        if(appctx==null){
            synchronized(this){
                if(appctx==null){
                    appctx=new GenericApplicationContext();
                }
            }
        }
        return appctx;
    }
}
