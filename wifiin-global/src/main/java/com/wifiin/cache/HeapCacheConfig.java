package com.wifiin.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;

public class HeapCacheConfig{
    private long maxSize=1000_0000;
    private int initSize=1024;
    private int concurrencyLevel=Runtime.getRuntime().availableProcessors();
    private long expirePeriod=5;
    private TimeUnit expireTimeUnit=TimeUnit.SECONDS;
    private RefType keyRef=RefType.STRONG;
    private RefType valueRef=RefType.STRONG;
    private Class<Weigher> weigherClass;
    private Weigher weigher;
    private long maxWeight=-1;
    public HeapCacheConfig(){}
    public long getMaxSize(){
        return maxSize;
    }
    public void setMaxSize(long maxSize){
        this.maxSize=maxSize;
    }
    public int getConcurrencyLevel(){
        return concurrencyLevel;
    }
    public void setConcurrencyLevel(int concurrencyLevel){
        this.concurrencyLevel=concurrencyLevel;
    }
    public long getExpirePeriod(){
        return expirePeriod;
    }
    public void setExpirePeriod(long expirePeriod){
        this.expirePeriod=expirePeriod;
    }
    public TimeUnit getExpireTimeUnit(){
        return expireTimeUnit;
    }
    public void setExpireTimeUnit(TimeUnit expireTimeUnit){
        this.expireTimeUnit=expireTimeUnit;
    }
    public int getInitSize(){
        return initSize;
    }
    public void setInitSize(int initSize){
        this.initSize=initSize;
    }
    public <K,V> HeapCacheConfig keyRefType(CacheBuilder<K,V> builder){
        keyRef.keyRefType(builder);
        return this;
    }
    public RefType getKeyRef(){
        return keyRef;
    }
    public void setKeyRef(RefType keyRef){
        this.keyRef=keyRef;
    }
    public <K,V> HeapCacheConfig valueRefType(CacheBuilder<K,V> builder){
        valueRef.valueRefType(builder);
        return this;
    }
    public RefType getValueRef(){
        return valueRef;
    }
    public void setValueRef(RefType valueRef){
        this.valueRef=valueRef;
    }
    @SuppressWarnings("rawtypes")
    public Class<Weigher> getWeigherClass(){
        return weigherClass;
    }
    @SuppressWarnings("rawtypes")
    public void setWeigherClass(Class<Weigher> weigherClass){
        this.weigherClass=weigherClass;
    }
    public Weigher getWeigher(){
        return weigher;
    }
    public void setWeigher(Weigher weigher){
        this.weigher=weigher;
    }
    public long getMaxWeight(){
        return maxWeight;
    }
    public void setMaxWeight(long maxWeight){
        this.maxWeight=maxWeight;
    }
    
}
