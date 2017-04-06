package com.wifiin.cron;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.wifiin.util.Help;

@EnableScheduling
public class CronTaskTrigger implements SchedulingConfigurer,ApplicationContextAware{
    private ApplicationContext appContext;
    public CronTaskTrigger() {}
     
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Collection<CronTask> tasks=appContext.getBeansOfType(CronTask.class).values();
        if(Help.isEmpty(tasks)){
            return;
        }
        tasks.forEach((task)->{
            taskRegistrar.addTriggerTask(task, (triggerContext)->{
                String cron=task.cron();
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            });
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException{
        this.appContext=appContext;
    }
}
