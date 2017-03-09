package com.wifiin.pool;

import java.util.concurrent.atomic.AtomicLong;

import com.wifiin.util.net.Localhost;
import com.wifiin.util.process.ProcessUtil;

public class Statistics{
    private int pid=ProcessUtil.getPid();
    private String ip=Localhost.getLocalIpInString();
    private AtomicLong trievedCount=new AtomicLong(0);
    private AtomicLong firstHitCount=new AtomicLong(0);
    private AtomicLong returnNullCount=new AtomicLong(0);
    private AtomicLong blockedCount=new AtomicLong(0);
    private AtomicLong maxTrieveConsumedMillis=new AtomicLong(0);
    private AtomicLong idleMillis=new AtomicLong(0);
    
    private AtomicLong returnedCount=new AtomicLong(0);
    private AtomicLong activeMillis=new AtomicLong(0);
    
    private AtomicLong tooFewCount=new AtomicLong(0);
    private AtomicLong tooFewExeCount=new AtomicLong(0);
    
    private AtomicLong retireCount=new AtomicLong(0);
    
    private AtomicLong retiredObjectCount=new AtomicLong(0);
    
    private AtomicLong createCount=new AtomicLong(0);
    
    /**
     * 退役执行次数
     */
    public void incrRetireCount(){
        retireCount.incrementAndGet();
    }
    /**
     * 退役对象数增一
     */
    public void incrRetiredObjectCount(){
        retiredObjectCount.incrementAndGet();
    }
    /**
     * 创建对象数增一
     */
    public void incrCreateCount(){
        createCount.incrementAndGet();
    }
    /**
     * TOO_FEW统计
     * @param exe
     */
    public void tooFewStat(boolean exe){
        incrTooFewCount();
        incrTooFewExeCount(exe);
    }
    /**
     * TOO_FEW触发次数
     */
    private void incrTooFewCount(){
        tooFewCount.incrementAndGet();
    }
    /**
     * TOO_FEW执行次数
     */
    private void incrTooFewExeCount(boolean exe){
        if(exe){
            tooFewExeCount.incrementAndGet();
        }
    }
    /**
     * 活跃对象返回统计
     * @param activeMillis
     * @param valid
     */
    public void returnStat(long activeMillis,boolean valid){
        addActiveMillis(activeMillis);
        incrReturnedCount(valid);
    }
    /**
     * 增加对象活跃毫秒数
     * @param activeMillis
     */
    private void addActiveMillis(long activeMillis){
        this.activeMillis.addAndGet(activeMillis);
    }
    /**
     * 有效返回次数增一。如果testOnReturn==true，就要先检查再调用。如果testOnReturn==false，一直传true
     * @param valid true: 有效返回，false：无效返回
     */
    private void incrReturnedCount(boolean valid){
        if(valid){
            returnedCount.incrementAndGet();
        }
    }
    /**
     * trieve 统计
     * @param firstHit 是否首次获取即命中
     * @param blocked 是否阻塞
     * @param blockMillis 本次trieve阻塞毫秒数
     * @param o 得到的对象
     */
    public void trieveStat(boolean firstHit,boolean blocked,long startTrieve,long lastTrieveMillis,long lastReturnedMillis,Object o){
        incrTrievedCount();
        incrFirstHitCount(firstHit);
        incrReturnNullCount(o);
        incrBlockedCount(blocked);
        replaceMaxTrieveConsumedMillis(lastTrieveMillis-startTrieve);
        incrIdleMillis(lastTrieveMillis-lastReturnedMillis);
    }
    /**
     * 增加空闲毫秒数
     * @param idleMillis
     */
    private void incrIdleMillis(long idleMillis){
        this.idleMillis.addAndGet(idleMillis);
    }
    /**
     * 只要是调用Pool.trieve()，trievedCount就自增1
     */
    private void incrTrievedCount(){
        trievedCount.incrementAndGet();
    }
    /**
     * 如果firstHit是true，首次获取命中次数自增1
     * @param firstHit
     */
    private void incrFirstHitCount(boolean firstHit){
        if(firstHit){
            firstHitCount.incrementAndGet();
        }
    }
    /**
     * 如果o不是null，returnNullCount自增1，o是从池得到的对象
     * @param o
     */
    private void incrReturnNullCount(Object o){
        if(o!=null){
            returnNullCount.incrementAndGet();
        }
    }
    /**
     * 增加block计数，一次增1
     */
    private void incrBlockedCount(boolean blocked){
        if(blocked){
            blockedCount.incrementAndGet();
        }
    }
    /**
     * 替换最新的最大阻塞时间
     * @param blockMillis
     * @return
     */
    private void replaceMaxTrieveConsumedMillis(long blockMillis){
        while(true){
            long current=maxTrieveConsumedMillis.get();
            if(current<blockMillis){
                if(maxTrieveConsumedMillis.compareAndSet(current,blockMillis)){
                    return;
                }
            }else{
                return;
            }
        }
    }
    public String getIp(){
        return ip;
    }
    public int getPid(){
        return pid;
    }
    public long getTrievedCount(){
        return trievedCount.get();
    }
    public long getMaxTrievedConsumedMillis(){
        return maxTrieveConsumedMillis.get();
    }
    public long getFirstHitCount(){
        return firstHitCount.get();
    }
    public long getIdleMillis(){
        return idleMillis.get();
    }
    public long getReturnedCount(){
        return returnedCount.get();
    }
    public long getActiveMillis(){
        return activeMillis.get();
    }
    public long getTooFewCount(){
        return tooFewCount.get();
    }
    public long getTooFewExeCount(){
        return tooFewExeCount.longValue();
    }
    public long getRetireCount(){
        return retireCount.get();
    }
    public long getRetiredObjectCount(){
        return retiredObjectCount.get();
    }
    public long getReturnNullCount(){
        return returnNullCount.get();
    }
    public long getBlockedCount(){
        return blockedCount.get();
    }
    public AtomicLong getCreateCount(){
        return createCount;
    }
    
}
