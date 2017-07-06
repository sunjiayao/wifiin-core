package com.wifiin.rpc.dubbo;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.wifiin.common.JSON;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class ProviderAccessLogFilter implements Filter{
    private static final Logger log=LoggerFactory.getLogger(ProviderAccessLogFilter.class);
    @Override
    public Result invoke(Invoker<?> invoker,Invocation invocation) throws RpcException{
        String prevSpan=invocation.getAttachment("prevSpan");
        String span=invocation.getAttachment("span");
        String trace=invocation.getAttachment("trace");
        String service=ThreadLocalStringBuilder.builder().append(invoker.getInterface().getName())
                .append("#").append(invocation.getMethodName()).toString();
        Object[] args=invocation.getArguments();
        LogContent logContent=new LogContent();
        logContent.arguments=args;
        logContent.prevSpan=prevSpan;
        logContent.span=span;
        logContent.trace=trace;
        logContent.service=service;
        Result result=null;
        long start=System.currentTimeMillis();
        try{
            if(log.isDebugEnabled()){
                log.debug("DubboProvider.start:"+JSON.toJSON(logContent));
            }
            result=invoker.invoke(invocation);
            logContent.result=result.getValue();
            logContent.consumed=System.currentTimeMillis()-start;
            log.info("DubboProvider:"+JSON.toJSON(logContent));
        }catch(Exception e){
            try{
                log.error("DubboProvider:"+JSON.toJSON(logContent),e);
            }catch(Exception e1){
                e.addSuppressed(e1);
            }
            throw new RpcException(e);
        }
        return result;
    }
    public static class LogContent{
        public String from=RpcContext.getContext().getRemoteAddressString();
        public String to=RpcContext.getContext().getLocalAddressString();
        public String application=RpcContext.getContext().getUrl().getParameter("application");
        public String service;
        public String trace;
        public String prevSpan;
        public String span;
        public Object[] arguments;
        public Object result;
        public long consumed;
    }
}
