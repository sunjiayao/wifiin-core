package com.wifiin.rpc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.dubbo.rpc.RpcContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.rpc.exception.RPCMethodGetterException;
import com.wifiin.util.Help;

public class RPCAccessAOP implements ApplicationContextAware{
    private static final Logger log=LoggerFactory.getLogger(RPCAccessAOP.class);
    private static final int SUCCESS_PRECHECK=1;
    private List<RPCPreAccessChecker> checkers;
    private ApplicationContext appContext;
    private static final Map<MethodTag,Method> METHOD_MAP=Maps.newConcurrentMap();
    public RPCAccessAOP(){}
    public void init(){
        Collection<RPCPreAccessChecker> checkers=appContext.getBeansOfType(RPCPreAccessChecker.class).values();
        if(Help.isNotEmpty(checkers)){
            this.checkers=Lists.newArrayList(checkers);
        }
    }
    private Integer preAccess(ProceedingJoinPoint point){
        int status=1;
        if(Help.isEmpty(checkers)){
            return null;
        }else{
            for(int i=0,l=checkers.size();i<l;i++){
                RPCPreAccessChecker checker=checkers.get(i);
                if(checker.needCheck(point)){
                    status=checker.check(point);
                    if(status!=SUCCESS_PRECHECK){
                        return status;
                    }
                }
            }
        }
        return SUCCESS_PRECHECK;
    }
    private Method getMethod(ProceedingJoinPoint point){
        String method=point.getSignature().getName();
        Object[] args=point.getArgs();
        Class[] methodArgTypes;
        if(Help.isEmpty(args)){
            methodArgTypes=new Class[0];
        }else{
            methodArgTypes=new Class[args.length];
            for(int i=0,l=args.length;i<l;i++){
                methodArgTypes[i]=args[i].getClass();
            }
        }
        MethodTag tag=new MethodTag(method,methodArgTypes);
        return METHOD_MAP.computeIfAbsent(tag,(t)->{
            try{
                return point.getTarget().getClass().getMethod(method,methodArgTypes);
            }catch(NoSuchMethodException | SecurityException e){
                throw new RPCMethodGetterException(e);
            }
        });
    }
    private RPCResponse createResponse(ProceedingJoinPoint point,int status) throws InstantiationException, IllegalAccessException{
        RPCResponse result=(RPCResponse)getMethod(point).getReturnType().newInstance();
        result.setStatus(status);
        return result;
    }
    public Object access(ProceedingJoinPoint point) throws Throwable{
        RPCResponse result=null;
        LogContent logContent=new LogContent();
        Exception ex=null;
        int status=1;
        try{
            long start=System.currentTimeMillis();
            status=preAccess(point);
            if(status==SUCCESS_PRECHECK){
                result=(RPCResponse)point.proceed();
            }else{
                result=createResponse(point,status);
            }
            logContent.consumed=System.currentTimeMillis()-start;
        }catch(Exception e){
            ex=e;
        }finally{
            logContent.args=point.getArgs();
            String rpc=point.getTarget().getClass().getName();
            logContent.rpc=rpc.substring(0,rpc.indexOf("$$EnhancerBySpringCGLIB"));
            logContent.method=point.getSignature().getName();
            if(result!=null){
                logContent.result=result;
            }
            if(ex==null){
                log.info("RPCAccess:"+GlobalObject.getJsonMapper().writeValueAsString(logContent));
            }else{
                try{
                    result=createResponse(point,status);
                }catch(Exception e){
                    ex.addSuppressed(e);
                    throw ex;
                }finally{
                    log.error("RPCAccess:"+GlobalObject.getJsonMapper().writeValueAsString(logContent),ex);
                }
            }
        }
        return result;
    }
    public static class LogContent{
        public Object[] args;
        public String rpc;
        public String method;
        public Object result;
        public long consumed;
        public String remoteHost=RpcContext.getContext().getRemoteAddressString();
        public String localhost=RpcContext.getContext().getLocalAddressString();
    }
    private class MethodTag{
        public String method;
        public Class[] methodArgTypes;
        public MethodTag(String method,Class[] methodArgTypes){
            this.method=method;
            this.methodArgTypes=methodArgTypes;
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException{
        this.appContext=context;
    }
}
