package com.wifiin.pool;

/**
 * 记录创建时间，最后借出时间，最后回池时间，在借出/返回时计算不在事件线程计算
 * @author Running
 *
 */
public class PooledObject<T>{
    /**
     * 池化对象
     */
    private T pooledObject;
    /**
     * 池对象被获取时是否首次命中
     */
    private boolean firstHit;
    /**
     * 池对象创建时间
     */
    private long createMillis;
    /**
     * 池对象的最后获取时间毫秒数
     */
    private long lastTrievedMillis;
    /**
     * 池对象的最后返回时间毫秒数
     */
    private long lastReturnedMillis;
    /**
     * 池对象工厂
     */
    private PooledObjectFactory<T> factory;
    public PooledObject(T o,PooledObjectFactory<T> factory){
        pooledObject=o;
        this.factory=factory;
        createMillis=System.currentTimeMillis();
    }
    @Override
    public void finalize() throws Throwable{
        try{
            factory.destroy(pooledObject);
        }catch(Exception t){}
        super.finalize();
    }
    public long getCreateMillis(){
        return createMillis;
    }
    public long getLastTrievedMillis(){
        return lastTrievedMillis;
    }
    public long getLastReturnedMillis(){
        return lastReturnedMillis==0?createMillis:lastReturnedMillis;
    }
    public void setCreateMillis(long createMillis){
        this.createMillis=createMillis;
    }
    public void setLastTrievedMillis(long lastTrievedMillis){
        this.lastTrievedMillis=lastTrievedMillis;
    }
    public void setLastReturnedMillis(long lastReturnedMillis){
        this.lastReturnedMillis=lastReturnedMillis;
    }
    public T getPooledObject(){
        return pooledObject;
    }
    public boolean getFirstHit(){
        return firstHit;
    }
    public void setFirstHit(boolean firstHit){
        this.firstHit=firstHit;
    }
}
