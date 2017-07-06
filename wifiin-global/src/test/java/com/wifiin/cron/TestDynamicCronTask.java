package com.wifiin.cron;

import java.util.Date;

import org.junit.Test;

public class TestDynamicCronTask{
//    @Test
    public void test() throws InterruptedException{
            new DynamicCronTaskScheduler().addOrReplace(new CronTask(){
                
                @Override
                public String name(){
                    return "test";
                }
                
                @Override
                public void execute(){
                    System.out.println(new Date());
                }
                
                @Override
                public String cron(){
                    return "0 */1 * * * ?";
                }
                
            });
        synchronized(TestDynamicCronTask.class){
            TestDynamicCronTask.class.wait();
        }
    }
}
