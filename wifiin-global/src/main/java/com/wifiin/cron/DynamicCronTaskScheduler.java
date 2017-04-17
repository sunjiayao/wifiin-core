package com.wifiin.cron;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.google.common.collect.Maps;
import com.wifiin.log.LoggerFactory;
import com.wifiin.util.ShutdownHookUtil;

public class DynamicCronTaskScheduler{
    private static final Logger log=LoggerFactory.getLogger(DynamicCronTaskScheduler.class);
    private ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler();
    private Map<String,ScheduledFuture<?>> scheudledFutures=Maps.newConcurrentMap();
    public DynamicCronTaskScheduler(){
        shutdownHook();
    }
    public DynamicCronTaskScheduler(
            int poolSize,String threadNamePrefix,
            boolean waitForJobsToCompleteOnShutdown,boolean daemon){
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor e)->{
            log.warn("RejectedExecutionHandler"+r,e);
        });
        scheduler.setWaitForTasksToCompleteOnShutdown(waitForJobsToCompleteOnShutdown);
        scheduler.setDaemon(daemon);
        shutdownHook();
    }
    private void shutdownHook(){
        ShutdownHookUtil.addHook(()->{
            scheudledFutures.values().forEach((sf)->{
                sf.cancel(true);
            });
            scheudledFutures.clear();
            scheduler.shutdown();
        });
    }
    public void addOrReplace(CronTask task){
        ScheduledFuture<?> sf=scheudledFutures.put(task.name(),scheduler.schedule(task,(triggerContext)->{
            return new CronTrigger(task.cron0()).nextExecutionTime(triggerContext);
        }));
        if(sf!=null){
            sf.cancel(true);
        }
    }
    public void cancel(String name){
        ScheduledFuture<?> sf=scheudledFutures.remove(name);
        if(sf!=null){
            sf.cancel(true);
        }
    }
    public void cancel(CronTask task){
        cancel(task.name());
    }
}
