package com.wifiin.springmvc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.exception.JsonGenerationException;
import com.wifiin.log.LoggerFactory;
import com.wifiin.reflect.BeanUtil;
import com.wifiin.util.Help;
import com.wifiin.util.ip.IPSeeker;

@Component
@Aspect
public class ControllerLogAspect{
    private static final Logger log=LoggerFactory.getLogger(ControllerLogAspect.class);
    @Around(value="@annotation(org.springframework.web.bind.annotation.RequestMapping) && @annotation(requestMapping)",argNames="requestMapping")
    public Object logAspect(ProceedingJoinPoint point, RequestMapping requestMapping){
        long start=System.currentTimeMillis();
        Object[] args=point.getArgs();
        Signature signature = point.getSignature();    
        MethodSignature methodSignature = (MethodSignature)signature;    
        Method targetMethod = methodSignature.getMethod();
        Object result=null;
        LogContent content=null;
        HttpServletRequest request=SpringMVCContext.getRequest();
        String uri=request.getRequestURI();
        String ip=IPSeeker.getIp(request);
        try{
            result=point.proceed();
            Object resultForLog=request.getAttribute(SpringMVCConstant.RESULT_FOR_LOG);
            content=new LogContent(System.currentTimeMillis()-start,args,resultForLog==null?result:resultForLog,uri,ip);
            log.info(content.toString());
        }catch(Throwable e){
            try{
                Class returnType=targetMethod.getReturnType();
                if(returnType.isAssignableFrom(Map.class)){
                    result=Maps.newHashMap();
                    Map map=(Map)result;
                    map.put("status",0);
                }else{
                    result=returnType.newInstance();
                    BeanUtil.set(result,"status",0,true);
                }
            }catch(InstantiationException | IllegalAccessException e1){
                e.addSuppressed(e1);
            }
            content=new LogContentWithException(System.currentTimeMillis()-start,args,result,uri,ip,e);
            log.warn(content.toString(),e);
        }
        return result;
    }
    private class LogContent{
        public long consumed;
        public Object params;
        public Object result;
        public String url;
        public String ip;
        public LogContent(long consumed,Object[] params,Object result,String url,String ip){
            this.consumed=consumed;
            this.params=ignoreServletObjectInArgs(params);
            if(result instanceof Result){
                Object logContent=((Result)result).getLog();
                if(logContent!=null){
                    this.result=logContent;
                }else{
                    this.result=result;
                }
            }else{
                this.result=result;
            }
            this.url=url;
            this.ip=ip;
        }
        private Object ignoreServletObjectInArgs(Object[] params){
            if(Help.isEmpty(params)){
                return null;
            }
            List args=Lists.newArrayList();
            for(int i=0,l=params.length;i<l;i++){
                Object arg=params[i];
                if(!(arg instanceof ServletRequest || arg instanceof ServletResponse)){
                    args.add(arg);
                }
            }
            if(Help.isEmpty(args)){
                return null;
            }
            return args.size()==1?args.get(0):args;
        }
        @Override
        public String toString(){
            try{
                return GlobalObject.getJsonMapper().writeValueAsString(this);
            }catch(JsonProcessingException e){
                throw new JsonGenerationException(e);
            }
        }
    }
    private class LogContentWithException extends LogContent{
        public String throwable;
        public LogContentWithException(long consumed,Object[] params,Object result,String url,String ip,Throwable throwable){
            super(consumed,params,result,url,ip);
            this.throwable=throwable.toString();
        }
    }
}
