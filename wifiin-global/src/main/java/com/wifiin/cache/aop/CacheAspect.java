package com.wifiin.cache.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wifiin.cache.HeapCache;
import com.wifiin.cache.aop.exception.CacheAOPException;
import com.wifiin.constant.WifiinConstant;
import com.wifiin.redis.RedisConnection;
import com.wifiin.util.Help;
import com.wifiin.util.text.template.TextTemplateFormatterFactory;

/**
 * 作为缓存key的格式化参数不能复合对象和基本类型对象混用。复合对象包括自定义对象、map、list、set，基本类型对象包括字符串、基本类型的包装类对象
 * @author Running
 *
 */
@Component
@Aspect
public class CacheAspect{
    private static final String[] EMPTY_STRING_ARRAY={""};
    @Autowired
    private RedisConnection redis;
    private HeapCache<String,Object> heapCache=HeapCache.<String,Object>getDefaultInstance("cacheAspect");
    @Around(value="@annotation(com.wifiin.cache.aop.Cacheable) && @annotation(cacheable)",argNames="cacheable")
    public Object cache(ProceedingJoinPoint point, Cacheable cacheable)throws Throwable{
        String key=key(cacheable.keyPattern(),cacheKeyArgs(cacheable.cacheKeyArgs(),point.getArgs()));
        if(cacheable.heapCache()){
            String k=key;
            return heapCache.get(k,()->{
                try{
                    return result(k,point,cacheable.format(),cacheable.expire());
                }catch(Throwable e){
                    throw new CacheAOPException(e);
                }
            });
        }else{
            return result(key,point,cacheable.format(),cacheable.expire());
        }
    }
    private Object cacheKeyArgs(int[] argIdxes,Object[] args){
        if(Help.isEmpty(args)){
            return EMPTY_STRING_ARRAY;
        }
        if(Help.isEmpty(argIdxes)){
            return args;
        }else if(argIdxes.length==1){
            Object arg=args[argIdxes[0]];
            if(arg instanceof String || arg instanceof Number || arg instanceof Boolean || arg instanceof Character){
                if(args.length==1){
                    arg=args;
                }else{
                    arg=new Object[]{arg};
                }
            }else{
                arg=args;
            }
            return arg;
        }else{
            Object[] arg=new Object[argIdxes.length];
            for(int i=0,l=argIdxes.length;i<l;i++){
                arg[i]=args[argIdxes[i]];
            }
            return arg;
        }
    }
    private String key(String pattern,Object arg){
        return TextTemplateFormatterFactory.getPlainTextTemplateFormatter(pattern).format(arg);
    }
    @SuppressWarnings("unchecked")
    private Object result(String key,ProceedingJoinPoint point,CachedDataFormat format,int expire) throws Throwable{
        String value=redis.get(key);
        if(Help.isEmpty(value)){
            Object result=point.proceed();
            if(Help.isNotEmpty(result)){
                value=format.format(result);
                redis.setex(key,expire==0?WifiinConstant.getCacheLifeSeconds():expire,value);
            }
            return result;
        }else{
            return format.parse(value,((MethodSignature)point.getSignature()).getReturnType());
        }
    }
    @Around(value="@annotation(com.wifiin.cache.aop.CacheEvict) && @annotation(evict)",argNames="evict")
    public Object evict(ProceedingJoinPoint point, CacheEvict evict)throws Throwable{
        Object result=point.proceed();
        String[] keys=evict.keyPattern();
        switch(evict.keyParams()){
        case RESULT:
            delKeys(keys,result,evict.heapCache());
        case ARGS:
            delKeys(keys,this.cacheKeyArgs(evict.cacheKeyArgs(),point.getArgs()),evict.heapCache());
        }
        return result;
    }
    private void delKeys(String[] keys,Object arg,boolean heapCache){
        for(int i=0,l=keys.length;i<l;i++){
            String key=key(keys[i],arg);
            redis.del(key);
            if(heapCache){
                this.heapCache.remove(key);
            }
        }
    }
}
