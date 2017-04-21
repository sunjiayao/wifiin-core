package com.wifiin.multilanguage.aop;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

import com.wifiin.cache.CacheKeyGenerator;
import com.wifiin.cache.HeapCache;
import com.wifiin.constant.WifiinConstant;
import com.wifiin.multilanguage.aop.exception.LanguageNotFoundException;
import com.wifiin.multilanguage.aop.exception.LanguageQueryException;
import com.wifiin.multilanguage.rpc.MultiLangRPC;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangData;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangResponse;
import com.wifiin.redis.RedisConnection;
import com.wifiin.reflect.BeanUtil;
import com.wifiin.util.Help;

@Component
@Aspect
public class MultiLangAspect{
    private static final String MULTI_LANG_CACHE_PREFIX="multiLang";
    private static final String LANG="lang";
    private static final String ID="id";
    private HeapCache<String,Map<String,Object>> heapCache=HeapCache.<String,Map<String,Object>>getDefaultInstance("multilang");

    @Lookup
    public RedisConnection redis(){
        return null;
    }
    @Lookup
    public MultiLangRPC multiLangRPC(){
        return null;
    }
    
    @Around(value="@annotation(com.wifiin.multilanguage.aop.MultiLangMethod) && @annotation(multilang)",argNames="multilang")
    public Object languageConvert(ProceedingJoinPoint point,MultiLangMethod multilang) throws Throwable{
        Object result=point.proceed();
        if(result!=null){
            String app=multilang.app();
            String multiLangKey=multiLangKey(multilang,result.getClass());
            String lang=lang(point);
            populate(result,app,multiLangKey,lang);
        }
        return result;
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private void populate(Object result,String app,String multiLangKey,String lang){
        if(result instanceof Collection){
            ((Collection)result).forEach((r)->{
                populate(r,app,multiLangKey,lang);
            });
        }else if(result instanceof Map){
            Map m=(Map)result;
            Map l=queryLang(app,multiLangKey,lang,m.get(ID));
            if(Help.isNotEmpty(l)){
                m.putAll(l);
            }
        }else{
            BeanUtil.populate(queryLang(app,multiLangKey,lang,BeanUtil.get(result,ID,false)),result,false,false);
        }
    }
    private Map<String,Object> queryLang(String app,String key,String lang,Object id){
        if(Help.isEmpty(id)){
            throw new LanguageQueryException("record id is not found from returned value");
        }
        String cacheKey=CacheKeyGenerator.generateKey(MULTI_LANG_CACHE_PREFIX,app,key,id,lang);
        return heapCache.get(key,()->{
            Map<String,?> result=null;
            result=redis().getJsonMap(cacheKey);
            if(Help.isEmpty(result)){
                MultiLangResponse response=multiLangRPC().queryLang(new MultiLangData(app,lang,key+'.'+id));
                if(response.getStatus()>0){
                    result = response.getFieldValues();
                    redis().setex(cacheKey,WifiinConstant.getCacheLifeSeconds(),response.getValue());
                }else{
                    throw new LanguageQueryException(response.getStatus());
                }
            }
            return Collections.unmodifiableMap(result);
        });
        
    }
    private String lang(ProceedingJoinPoint point){
        String lang=null;
        Object[] args=point.getArgs();
        MethodSignature ms=(MethodSignature)point.getSignature();
        Class[] argTypes=ms.getMethod().getParameterTypes();
        Annotation[][] argAnnotations=ms.getMethod().getParameterAnnotations();
        for(int i=0,l=args.length;i<l && Help.isEmpty(lang);i++){
            Object arg=args[i];
            Annotation[] annotations=argAnnotations[i];
            for(int j=0,jl=annotations.length;j<jl;j++){
                Annotation annotation=annotations[j];
                if(annotation instanceof Lang && arg!=null){
                    lang=arg.toString();
                }
            }
            if(Help.isEmpty(lang) && !(arg instanceof String || arg instanceof Number || arg instanceof Boolean || arg instanceof Character)){
                Object v=BeanUtil.get(arg,LANG,true);
                if(Help.isNotEmpty(v)){
                    lang=v.toString();
                }
            }
        }
        if(Help.isEmpty(lang)){
            throw new LanguageNotFoundException("there is exact one arg of method should be declared with annotation com.wifiin.multilanguage.Lang, or at least one property of args named lang");
        }
        return lang;
    }
    private String multiLangKey(MultiLangMethod multilang,Class returnType){
        String multiLangKey=multilang.value();
        if(Help.isNotEmpty(multiLangKey)){
            return multiLangKey;
        }
        MultiLangMapper multiLangMapper=(MultiLangMapper)returnType.getAnnotation(MultiLangMapper.class);
        if(multiLangMapper!=null){
            String key=multiLangMapper.value();
            if(Help.isNotEmpty(key)){
                multiLangKey=key;
            }
        }
        if(Help.isEmpty(multiLangKey)){
            multiLangKey=returnType.getName();
        }
        return multiLangKey;
    }
}
