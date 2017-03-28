package com.wifiin.rpc.dubbo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.util.Help;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.net.Localhost;
import com.wifiin.util.process.ProcessUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class ConsumerAccessLogFilter implements Filter{
    private static final Logger log=LoggerFactory.getLogger(ConsumerAccessLogFilter.class);
    private static final AtomicLong SEQUENCE=new AtomicLong(0);
    private static final Map<String,String> SPAN_MAP=Maps.newConcurrentMap();
    @Override
    public Result invoke(Invoker<?> invoker,Invocation invocation) throws RpcException{
        RpcContext context=RpcContext.getContext();
        long now=System.currentTimeMillis();
        String trace=trace(context,now);
        String application = context.getUrl().getParameter("application");
        String prevSpan=context.getAttachment("span");
        String service=ThreadLocalStringBuilder.builder().append(invoker.getInterface().getName()).append('#').append(invocation.getMethodName()).toString();
        String span=SPAN_MAP.computeIfAbsent(service,(s)->{
            try{
                return new BigInteger(1,MessageDigestUtil.md5(s)).toString(36).toUpperCase();
            }catch(UnsupportedEncodingException e){
                return null;
            }
        });
        context.setAttachment("prevSpan",prevSpan);
        context.setAttachment("span",span);
        LogContent logContent=new LogContent();
        logContent.from=context.getLocalAddressString();
        logContent.to=context.getRemoteAddressString();
        logContent.application=application;
        logContent.service=service;
        logContent.trace=trace;
        logContent.prevSpan=prevSpan;
        logContent.span=span;
        logContent.arguments=invocation.getArguments();
        try{
            Result result=invoker.invoke(invocation);
            logContent.result=result.getValue();
            logContent.consumed=System.currentTimeMillis()-now;
            log.info("DubboConsumer:"+GlobalObject.getJsonMapper().writeValueAsString(logContent));
            return result;
        }catch(Exception e){
            try{
                log.error("DubboConsumer:"+GlobalObject.getJsonMapper().writeValueAsString(logContent),e);
            }catch(JsonProcessingException e1){
                e.addSuppressed(e1);
            }
            throw new RpcException(e);
        }
    }
    private String trace(RpcContext context,long now){
        String trace=context.getAttachment("trace");
        if(Help.isEmpty(trace)){
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(DataOutputStream out=new DataOutputStream(bytes)){
                out.writeLong(now);
                out.write(Localhost.getLocalMacInBytes());
                out.writeShort(ProcessUtil.getPid());
                out.writeLong(SEQUENCE.getAndIncrement());
                out.flush();
            }catch(IOException e){}
            trace=new BigInteger(1,bytes.toByteArray()).toString(36).toUpperCase();
            context.setAttachment("trace",trace);
        }
        return trace;
    }
    public static class LogContent{
        public String from=RpcContext.getContext().getLocalAddressString();
        public String to=RpcContext.getContext().getRemoteAddressString();
        public String application;
        public String service;
        public String trace;
        public String prevSpan;
        public String span;
        public Object[] arguments;
        public Object result;
        public long consumed;
    }
}
