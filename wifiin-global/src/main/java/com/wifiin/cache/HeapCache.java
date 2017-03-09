package com.wifiin.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.collect.Maps;
import com.wifiin.cache.exception.CacheException;
import com.wifiin.config.ConfigManager;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class HeapCache<K,V>{
    private static final String CACHE_INIT_PARAMS="wifiin.cache.init.params";
    private Cache<K,V> cache;
    private static Map<String,HeapCache<?,?>> instance=Maps.newConcurrentMap();
    public static <K,V> HeapCache<K,V> getInstance(String cacheName){
        return getInstance(cacheName,()->{
            return ConfigManager.getInstance().getObject(
                    ThreadLocalStringBuilder.builder().append(CACHE_INIT_PARAMS).append('.').append(cacheName).toString(),
                    HeapCacheConfig.class,
                    ConfigManager.getInstance().getObject(CACHE_INIT_PARAMS,HeapCacheConfig.class,new HeapCacheConfig()));
        });
    }
    public static <K,V> HeapCache<K,V> getDefaultInstance(String cacheName){
        return getInstance(cacheName,HeapCacheConfig::new);
    }
    public static <K,V> HeapCache<K,V> getInstance(String cacheName,HeapCacheConfig config){
        return getInstance(cacheName,()->{
            return config;
        });
    }
    @SuppressWarnings("unchecked")
    public static <K,V> HeapCache<K,V> getInstance(String cacheName,Supplier<HeapCacheConfig> configSupplier){
        return (HeapCache<K,V>)instance.computeIfAbsent(cacheName,(k)->{
            return new HeapCache<K,V>(configSupplier.get());
        });
    }
    
    @SuppressWarnings({"rawtypes","unchecked"})
    private HeapCache(HeapCacheConfig config){
        CacheBuilder builder=CacheBuilder.newBuilder()
                .concurrencyLevel(config.getConcurrencyLevel());
        Class<Weigher> weigherClass=config.getWeigherClass();
        Weigher weigher=config.getWeigher();
        if(weigherClass!=null || weigher!=null){
            if(config.getMaxWeight()>=0){
                builder.maximumWeight(config.getMaxWeight());
            }else{
                throw new CacheException("maxWeight and weigherClass must be specified both or neither");
            }
            try{
                builder.weigher(weigherClass!=null?weigherClass.newInstance():weigher);
            }catch(InstantiationException | IllegalAccessException e){
                throw new CacheException(e);
            }
        }else{
            builder.maximumSize(config.getMaxSize())
                   .initialCapacity(config.getInitSize())
                   .expireAfterWrite(config.getExpirePeriod(),config.getExpireTimeUnit());
        }
        config.keyRefType(builder).valueRefType(builder);
        cache=builder.build();
    }
    @SuppressWarnings("unchecked")
    public <T> T get(String key){
        return (T)cache.getIfPresent(key);
    }
    /**
     * 获取缓存数据，如果没有缓存key就执行loader，loader的执行结果会保存在缓存中并作为本次调用的返回值，如果找到了缓存的对象就返回这个对象不执行laoder。
     * @param key
     * @param loader
     * @return
     */
    public V get(K key,Callable<V> loader){
        try{
            return cache.get(key,loader);
        }catch(ExecutionException e){
            throw new CacheException(e);
        }
    }
    public void put(K key, V value){
        cache.put(key,value);
    }
    @SuppressWarnings("unchecked")
    public V putIfAbsent(K key,V value){
        return cache.asMap().putIfAbsent(key,value);
    }
    public void remove(String key){
        cache.invalidate(key);
    }
    public void remove(String... keys){
        remove(Arrays.stream(keys));
    }
    public void remove(Collection<String> keys){
        remove(keys.stream());
    }
    private void remove(Stream<String> stream){
        stream.forEach((k)->{
            remove(k);
        });
    }
    public long size(){
        return cache.size();
    }
    public static void main(String[] args) throws InterruptedException{
        HeapCacheConfig config=new HeapCacheConfig();
        config.setValueRef(RefType.WEAK);
        config.setConcurrencyLevel(Runtime.getRuntime().availableProcessors());
        config.setExpireTimeUnit(TimeUnit.MILLISECONDS);
//        config.setWeigher((k,v)->{return 0;});
//        config.setMaxWeight(0);
        HeapCache c=getInstance("",config);
        ExecutorService es=Executors.newFixedThreadPool(10);
        CountDownLatch latch=new CountDownLatch(10);
        for(int i=0;i<10;i++){
            es.submit(()->{
                for(int j=0;j<10000;j++){
                    long id=Thread.currentThread().getId();
                    System.out.println(id+"  "+"  "+c.size()+"  "+c.get(ThreadLocalRandom.current().nextInt(100),()->{return id;}));
                }
                latch.countDown();
            });
        }
        latch.await();
        System.gc();
        System.gc();
        System.gc();
        System.out.println(c.size());
    }
}