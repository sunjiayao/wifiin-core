package com.wifiin.cron;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.google.common.collect.Maps;

public class DynamicCronTaskScheduler{
    private ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler();
    private Map<String,ScheduledFuture<?>> scheudledFutures=Maps.newConcurrentMap();
    public DynamicCronTaskScheduler(){}
    public DynamicCronTaskScheduler(int poolSize){
        scheduler.setPoolSize(poolSize);
    }
    public void addOrReplace(CronTask task){
        ScheduledFuture<?> sf=scheudledFutures.put(task.name(),scheduler.schedule(task,(triggerContext)->{
            return new CronTrigger(task.cron()).nextExecutionTime(triggerContext);
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
