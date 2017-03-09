package com.wifiin.pool;

public interface KeyPooledObjectFactory<K extends PoolKey,T> extends PooledObjectFactory<T>{
    public void setPoolKey(K key);
    
}
