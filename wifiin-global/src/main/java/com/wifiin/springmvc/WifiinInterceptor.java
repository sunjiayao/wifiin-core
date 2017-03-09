package com.wifiin.springmvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.Decider;
import com.wifiin.common.GlobalObject;

public class WifiinInterceptor implements HandlerInterceptor{
    private static final Logger log=LoggerFactory.getLogger(WifiinInterceptor.class);
    
    private static final String START="start";
    
    private Decider logDecider;
    public Decider getLogDecider(){
        return logDecider;
    }
    public void setLogDecider(Decider logDecider){
        this.logDecider = logDecider;
    }
    private boolean decideLog(String url){
        return logDecider==null || logDecider.decide(url);
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception)
            throws Exception{
        if(exception==null){
            LogContent lc=new LogContent(
                    System.currentTimeMillis()-(Long)request.getAttribute(START),
                    request.getAttribute(SpringMVCConstant.REQUEST_PARAMS),
                    request.getAttribute(SpringMVCConstant.RESULT_FOR_LOG),
                    request.getServletPath(),
                    SpringMVCContext.getIp(request));
            String logContent=GlobalObject.getJsonMapper().writeValueAsString(lc);
            log.info(logContent);
        }else{
            LogContentWithException lc=new LogContentWithException(
                    System.currentTimeMillis()-(Long)request.getAttribute(START),
                    request.getAttribute(SpringMVCConstant.REQUEST_PARAMS),
                    request.getAttribute(SpringMVCConstant.RESULT_FOR_LOG),
                    request.getServletPath(),
                    SpringMVCContext.getIp(request),
                    exception);
            String logContent=GlobalObject.getJsonMapper().writeValueAsString(lc);
            log.error(logContent,exception);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv)
            throws Exception{
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception{
        request.setAttribute(START, System.currentTimeMillis());
        return true;
    }
    
    private class LogContent{
        public long consumed;
        public Object params;
        public Object result;
        public String url;
        public String ip;
        public LogContent(long consumed,Object params,Object result,String url,String ip){
            this.consumed=consumed;
            this.params=params;
            if(decideLog(url)){
                this.result=result;
            }
            this.url=url;
            this.ip=ip;
        }
    }
    private class LogContentWithException extends LogContent{
        public String throwable;
        public LogContentWithException(long consumed,Object params,Object result,String url,String ip,Exception throwable){
            super(consumed,params,result,url,ip);
            this.throwable=throwable.toString();
        }
    }
    public static void main(String[] args) throws JsonProcessingException, InterruptedException{
        int threads=Runtime.getRuntime().availableProcessors()*2;
        System.out.println(threads);
        ExecutorService es=Executors.newFixedThreadPool(threads);
        final CountDownLatch cdl=new CountDownLatch(threads);
        AtomicLong la=new AtomicLong(0);
        long s=System.currentTimeMillis();
        for(int i=0;i<1000_0000;i++){
            es.execute(()->{
                la.incrementAndGet();
                cdl.countDown();
            });
        }
        cdl.await();
        System.out.println(System.currentTimeMillis()-s);
        CountDownLatch cdl2=new CountDownLatch(1000);
        s=System.currentTimeMillis();
        for(int i=0;i<1000_0000;i++){
            es.execute(()->{
                System.currentTimeMillis();
                cdl2.countDown();
            });
        }
        cdl.await();
        System.out.println(System.currentTimeMillis()-s);
    }
}
