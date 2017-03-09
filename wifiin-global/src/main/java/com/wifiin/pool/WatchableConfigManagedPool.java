package com.wifiin.pool;

import java.util.function.Function;

import com.wifiin.config.ConfigManager;
/**
 * 从配置管理工具得到配置参数构造Pool<T>对象
 * @author Running
 *
 * @param <T>
 */
public class WatchableConfigManagedPool<T> implements PoolCommand<T>{
    private Pool<T> pool;
    @SuppressWarnings({"rawtypes","unchecked"})
    public WatchableConfigManagedPool(String key){
        buildPool(ConfigManager.getInstance().getObject(key,PoolConfig.class,new PoolConfig(),this::buildPool));
    }
    private Pool<T> buildPool(PoolConfig<T> config){
        Pool<T> prev=pool;
        Pool<T> newOne=null;
        try{
            newOne=new Pool<T>(config);
            return pool=newOne;
        }finally{
            if(newOne!=null){
                prev.shutdown();
            }
        }
    }
    @Override
    public <R> R execute(Function<T,R> fn){
        return pool.execute(fn);
    }
    @Override
    public T trieve(){
        return pool.trieve();
    }
    @Override
    public boolean returnObject(T t){
        return pool.returnObject(t);
    }
    @Override
    public void shutdown(){
        pool.shutdown();
    }
}
