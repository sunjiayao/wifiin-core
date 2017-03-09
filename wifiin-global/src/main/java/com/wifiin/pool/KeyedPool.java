package com.wifiin.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.ServiceStatus;
import com.wifiin.loadbalance.strategy.MurmurHashTreeStrategy;
import com.wifiin.pool.exception.PoolException;
import com.wifiin.util.Help;

public class KeyedPool<K extends PoolKey,T> implements KeyedPoolCommand<T>, Iterable<KeyedPool<K,T>.PoolWithKey>{
    private ServiceCollection<PoolWithKey,MurmurHashTreeStrategy<PoolWithKey>> pools;
    @SuppressWarnings({"unchecked","rawtypes"})
    public KeyedPool(KeyedPoolConfig<K,T> config){
        List<PoolWithKey> pools=new ArrayList<>(config.getKeys().size()*2);
        config.getKeys().forEach((k)->{
            pools.add(new PoolWithKey(k,config));
        });
        this.pools=new ServiceCollection(pools,new MurmurHashTreeStrategy());
    }
    public class PoolWithKey extends Pool<T> implements Service{
        public PoolWithKey(K key,KeyedPoolConfig<K,T> config){
            super(config);
            try{
                Class<KeyPooledObjectFactory<K,T>> factoryClass=config.getPooledObjectFactoryClass();
                KeyPooledObjectFactory<K,T> factory=factoryClass.newInstance();
                factory.setPoolKey(key);
                factory.setPoolConfig((PoolConfig<T>)config);
                super.setPooledObjectFactory(factory);
                this.key=key;
                this.name=Help.convert(key.name(),"");//每次计算一致性哈希的结果必须一致，不能使用任何随机性质的字符串或数值
            }catch(Exception e){
                throw new PoolException(e);
            }
        }
        private String name;
        private K key;
        @Override
        public long weight(){
            return 160*key.weight();
        }
        @Override
        public String name(){
            return name;
        }
        @Override
        public void notify(ServiceStatus status){
            // do nothing
        }
    }
    private Pool<T> pool(String key){
        return (Pool<T>)pools.get(key);
    }
    public T trieve(String key){
        return pool(key).trieve();
    }
    public boolean returnKeyedObject(String key,T o){
        return pool(key).returnObject(o);
    }
    public <R> R execute(String key,BiFunction<String,T,R> executor){
        T o=null;
        try{
            o=trieve(key);
            return executor.apply(key,o);
        }finally{
            returnKeyedObject(key,o);
        }
    }
    @Override
    public void shutdown(){
        pools.iterator().forEachRemaining((p)->{
            p.shutdown();
        });
    }
    @Override
    public Iterator<KeyedPool<K,T>.PoolWithKey> iterator(){
        return (Iterator<KeyedPool<K,T>.PoolWithKey>)(pools.iterator());
    }
}
