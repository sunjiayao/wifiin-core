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
/**
 * 动态生成定时调度任务
 * @author Running
 *
 */
public class DynamicCronTaskScheduler{
    private static final Logger log=LoggerFactory.getLogger(DynamicCronTaskScheduler.class);
    private ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler();
    private Map<String,ScheduledFuture<?>> scheudledFutures=Maps.newConcurrentMap();
    public DynamicCronTaskScheduler(){
        shutdownHook();
    }
    /**
     * 
     * @param poolSize    定时任务线程池大小
     * @param threadNamePrefix  定时任务线程名前缀
     * @param waitForJobsToCompleteOnShutdown 关闭时是否等待定时任务完成
     * @param daemon 定时任务线程是否守护线程
     */
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
    /**
     * 添加或删除定时任务
     * @param task
     */
    public void addOrReplace(CronTask task){
        ScheduledFuture<?> sf=scheudledFutures.put(task.name(),scheduler.schedule(task,(triggerContext)->{
            return new CronTrigger(task.cron0()).nextExecutionTime(triggerContext);
        }));
        if(sf!=null){
            sf.cancel(true);
        }
    }
    /**
     * 取消定时任务
     * @param name 定时任务名称
     */
    public void cancel(String name){
        ScheduledFuture<?> sf=scheudledFutures.remove(name);
        if(sf!=null){
            sf.cancel(true);
        }
    }
    /**
     * 取消定时任务
     * @param task
     */
    public void cancel(CronTask task){
        cancel(task.name());
    }
    /**
     * 取消全部任务
     */
    public void cancelAll(){
        scheudledFutures.values().forEach((sf)->{
            sf.cancel(true);
        });
    }
    /**
     * 关闭
     */
    public void shutdown(){
        cancelAll();
        scheudledFutures.clear();
        scheduler.shutdown();
    }
}
