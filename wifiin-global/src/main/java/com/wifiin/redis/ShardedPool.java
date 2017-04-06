package com.wifiin.redis;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Hashing;
import redis.clients.util.ShardInfo;

public abstract class ShardedPool<K extends ShardInfo<T>,T> extends Sharded<T,K>{
    protected GenericKeyedObjectPool<K,T> internalPool;

    /**
     * Using this constructor means you have to set and initialize the
     * internalPool yourself.
     */
    public ShardedPool(List<K> shards, Hashing algo, Pattern tagPattern) {
		super(shards, algo, tagPattern);
	}
	public ShardedPool(List<K> shards, Hashing algo) {
		super(shards, algo);
	}
	public ShardedPool(List<K> shards, Pattern tagPattern) {
		super(shards, tagPattern);
	}
	public ShardedPool(List<K> shards) {
		super(shards);
	}
	
	public ShardedPool(List<K> shards, Hashing algo, Pattern tagPattern,final GenericKeyedObjectPoolConfig poolConfig,KeyedPooledObjectFactory<K,T> factory) {
		super(shards, algo, tagPattern);
		initPool(poolConfig, factory);
	}
	public ShardedPool(List<K> shards, Hashing algo,final GenericKeyedObjectPoolConfig poolConfig,KeyedPooledObjectFactory<K,T> factory) {
		super(shards, algo);
		initPool(poolConfig, factory);
	}
	public ShardedPool(List<K> shards, Pattern tagPattern,final GenericKeyedObjectPoolConfig poolConfig,KeyedPooledObjectFactory<K,T> factory) {
		super(shards, tagPattern);
		initPool(poolConfig, factory);
	}
	public ShardedPool(List<K> shards,final GenericKeyedObjectPoolConfig poolConfig,KeyedPooledObjectFactory<K,T> factory) {
		super(shards);
		initPool(poolConfig, factory);
	}
	public void initPool(final GenericKeyedObjectPoolConfig poolConfig,KeyedPooledObjectFactory<K,T> factory) {
    	if (this.internalPool != null) {
    		try {
    			closeInternalPool();
    		} catch (Exception e) {}
    	}
    	this.internalPool = new GenericKeyedObjectPool<K,T>(factory, poolConfig);
	}
	
	public T getResource(String key){
		return getResource(super.getShardInfo(key));
	}
    public T getResource(K key) {
		try {
		    return internalPool.borrowObject(key);
		} catch (Exception e) {
		    throw new JedisConnectionException("Could not get a resource from the pool", e);
		}
    }
    
    public void returnResourceObject(final String key, final T resource){
    	this.returnBrokenResource(super.getShardInfo(key), resource);
    }
    public void returnResourceObject(final K key,final T resource) {
		try {
		    internalPool.returnObject(key,resource);
		} catch (Exception e) {
		    throw new JedisException("Could not return the resource to the pool", e);
		}
    }
    
    public void returnBrokenResource(final String key, final T resource){
    	this.returnBrokenResource(super.getShardInfo(key), resource);
    }
    public void returnBrokenResource(final K key,final T resource) {
    	returnBrokenResourceObject(key,resource);
    }

    public void returnResource(final String key, final T resource){
    	this.returnResource(super.getShardInfo(key), resource);
    }
    public void returnResource(final K key,final T resource) {
    	returnResourceObject(key,resource);
    }
    public void addObject(K key) throws Exception{
    	this.internalPool.addObject(key);
    }
    public void clear(K key){
    	this.internalPool.clear(key);
    }
    
    public void destroy() {
    	closeInternalPool();
    }
    
    protected void returnBrokenResourceObject(final String key, final T resource){
    	this.returnBrokenResourceObject(super.getShardInfo(key), resource);
    }
    protected void returnBrokenResourceObject(final K key,final T resource) {
		try {
		    internalPool.invalidateObject(key,resource);
		} catch (Exception e) {
		    throw new JedisException("Could not return the resource to the pool", e);
		}
    }

    protected void closeInternalPool() {
		try {
		    internalPool.close();
		} catch (Exception e) {
		    throw new JedisException("Could not destroy the pool", e);
		}
    }
}
