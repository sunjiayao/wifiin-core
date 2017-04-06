package com.wifiin.cron;

import java.io.IOException;
import java.util.Date;

import org.apache.curator.framework.recipes.leader.LeaderLatch;

import com.wifiin.config.ConfigManager;
import com.wifiin.cron.exception.CronException;

public abstract class ZookeeperCronTask implements CronTask{
    private ConfigManager zkManager;
    private String leaderPath;
    private String executedAtPath;
    private LeaderLatch latch;
    private String cronKey;
    public ZookeeperCronTask(String initKey){
        this(ConfigManager.getInstance().getObject(initKey,ZookeeperCronTaskInitParams.class,new ZookeeperCronTaskInitParams()));
    }
    public ZookeeperCronTask(ZookeeperCronTaskInitParams params){
        this(params.getNamespace(),params.getRetryIntervalMs(),params.getConnectString(),params.getLeaderPath(),params.getExecutedAtPath(),params.getCronKey());
    }
    public ZookeeperCronTask(String namespace,int retryIntervalMs,String connectString,String leaderPath,String executedAtPath,String cronKey){
        zkManager=new ConfigManager(namespace,connectString,retryIntervalMs);
        this.leaderPath=leaderPath;
        this.cronKey=cronKey;
    }
    public boolean executing(){
        if(latch==null){
            latch=new LeaderLatch(zkManager.curator(),leaderPath);
            return !latch.hasLeadership();
        }else{
            return false;
        }
    }
    public void clean(){
        if(latch!=null){
            try{
                latch.close();
            }catch(IOException e){
                throw new CronException(e);
            }
            latch=null;
        }
    }
    public void executedAt(Date executedTime){
        zkManager.setDataOrCreate(executedAtPath,executedTime.getTime());
    }
    public String cron(){
        return ConfigManager.getInstance().getString(cronKey);
    }
}
