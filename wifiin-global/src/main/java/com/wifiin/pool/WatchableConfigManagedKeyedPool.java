package com.wifiin.pool;

import java.util.Iterator;
import java.util.function.BiFunction;

import com.wifiin.config.ConfigManager;
/**
 * 受从配置管理工具得到配置参数构造KeyedPool<K,T>对象
 * @author Running
 *
 * @param <K>
 * @param <T>
 */
public class WatchableConfigManagedKeyedPool<K extends PoolKey,T> implements KeyedPoolCommand<T>,Iterable<KeyedPool<K,T>.PoolWithKey>{
    private KeyedPool<K,T> pool;
    @SuppressWarnings("unchecked")
    public WatchableConfigManagedKeyedPool(String key){
        buildPool(ConfigManager.getInstance().getObject(key,KeyedPoolConfig.class,new KeyedPoolConfig<>(),this::buildPool));
    }
    private KeyedPool<K,T> buildPool(KeyedPoolConfig<K,T> config){
        KeyedPool<K,T> prev=pool;
        KeyedPool<K,T> newOne=null;
        try{
            newOne=new KeyedPool<K,T>(config);
            return pool=newOne;
        }finally{
            if(newOne!=null){
                prev.shutdown();
            }
        }
    }
    @Override
    public <R> R execute(String key,BiFunction<String,T,R> executor){
        return pool.execute(key,executor);
    }

    @Override
    public boolean returnKeyedObject(String key,T o){
        return pool.returnKeyedObject(key,o);
    }

    @Override
    public T trieve(String key){
        return pool.trieve(key);
    }
    @Override
    public void shutdown(){
        pool.shutdown();
    }
    @Override
    public Iterator<KeyedPool<K,T>.PoolWithKey> iterator(){
        return pool.iterator();
    }
}
