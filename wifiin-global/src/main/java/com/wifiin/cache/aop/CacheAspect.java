package com.wifiin.cache.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wifiin.cache.HeapCache;
import com.wifiin.cache.aop.exception.CacheAOPException;
import com.wifiin.redis.RedisConnection;
import com.wifiin.util.Help;
import com.wifiin.util.text.template.TextTemplateFormatterFactory;

@Component
@Aspect
public class CacheAspect{
    @Autowired
    private RedisConnection redis;
    private HeapCache<String,Object> heapCache=HeapCache.<String,Object>getDefaultInstance("cacheAspect");
    @Around(value="@annotation(com.wifiin.cache.aop.Cacheable) && @annotation(cacheable)",argNames="cacheable")
    public Object cache(ProceedingJoinPoint point, Cacheable cacheable)throws Throwable{
        String key=key(cacheable.keyPattern(),point.getArgs());
        if(cacheable.heapCache()){
            String k=key;
            return heapCache.get(k,()->{
                try{
                    return result(k,point,cacheable);
                }catch(Throwable e){
                    throw new CacheAOPException(e);
                }
            });
        }else{
            return result(key,point,cacheable);
        }
    }
    private String key(String pattern,Object[] args){
        if(Help.isNotEmpty(args)){
            if(args.length==1){
                return key(pattern,args[0]);
            }else{
                return key(pattern,args);
            }
        }
        return pattern;
    }
    private String key(String pattern,Object arg){
        return TextTemplateFormatterFactory.getPlainTextTemplateFormatter(pattern).format(arg);
    }
    private Object result(String key,ProceedingJoinPoint point,Cacheable params) throws Throwable{
        String value=redis.get(key);
        if(Help.isEmpty(value)){
            Object result=point.proceed();
            value=params.format().format(result);
            redis.setex(key,params.expire(),value);
            return result;
        }else{
            return params.format().parse(value,((MethodSignature)point.getSignature()).getReturnType());
        }
    }
    @After(value="@annotation(com.wifiin.cache.aop.CacheEvict) && @annotation(evict)",argNames="evict")
    public Object evict(ProceedingJoinPoint point, CacheEvict evict)throws Throwable{
        Object result=point.proceed();
        String[] keys=evict.keyPattern();
        switch(evict.keyParams()){
        case RESULT:
            delKeys(keys,result,evict.heapCache());
        case ARGS:
            delKeys(keys,point.getArgs(),evict.heapCache());
        }
        return result;
    }
    private void delKeys(String[] keys,Object arg,boolean heapCache){
        for(int i=0,l=keys.length;i<l;i++){
            String key=key(keys[i],arg);
            if(heapCache){
                this.heapCache.remove(key);
            }
            redis.del(key);
        }
    }
}
