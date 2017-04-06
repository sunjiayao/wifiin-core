package com.wifiin.cron;

import java.io.Serializable;

public class ZookeeperCronTaskInitParams implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=2376114547063098318L;
    private String namespace;
    private int retryIntervalMs;
    private String connectString;
    private String leaderPath;
    private String executedAtPath;
    private String cronKey;
    public ZookeeperCronTaskInitParams(){}
    public String getNamespace(){
        return namespace;
    }
    public void setNamespace(String namespace){
        this.namespace=namespace;
    }
    public int getRetryIntervalMs(){
        return retryIntervalMs;
    }
    public void setRetryIntervalMs(int retryIntervalMs){
        this.retryIntervalMs=retryIntervalMs;
    }
    public String getConnectString(){
        return connectString;
    }
    public void setConnectString(String connectString){
        this.connectString=connectString;
    }
    public String getLeaderPath(){
        return leaderPath;
    }
    public void setLeaderPath(String leaderPath){
        this.leaderPath=leaderPath;
    }
    public String getExecutedAtPath(){
        return executedAtPath;
    }
    public void setExecutedAtPath(String executedAtPath){
        this.executedAtPath=executedAtPath;
    }
    public String getCronKey(){
        return cronKey;
    }
    public void setCronKey(String cronKey){
        this.cronKey=cronKey;
    }
    
}
