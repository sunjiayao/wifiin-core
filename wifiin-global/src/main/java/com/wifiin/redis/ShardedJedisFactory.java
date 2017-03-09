package com.wifiin.redis;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.clients.jedis.Jedis;

public class ShardedJedisFactory implements KeyedPooledObjectFactory<JedisShardInfo,Jedis>{
	@Override
	public void activateObject(JedisShardInfo key, PooledObject<Jedis> pooledShardedJedis)throws Exception {}
	@Override
	public void destroyObject(JedisShardInfo key, PooledObject<Jedis> pooledShardedJedis)throws Exception {
		final Jedis jedis = pooledShardedJedis.getObject();
	    try {
		    try {
		    	jedis.quit();
		    } catch (Exception e) {}
		    jedis.disconnect();
		} catch (Exception e) {}
	}
	@Override
	public PooledObject<Jedis> makeObject(JedisShardInfo key)throws Exception {
	    return new DefaultPooledObject<Jedis>(key.createResource());
	}
	@Override
	public void passivateObject(JedisShardInfo key,PooledObject<Jedis> pooledShardedJedis) throws Exception {}
	@Override
	public boolean validateObject(JedisShardInfo key,PooledObject<Jedis> pooledShardedJedis) {
		try {
			Jedis jedis = pooledShardedJedis.getObject();
			return jedis.ping().equals("PONG");
		} catch (Exception ex) {
			return false;
		}
	}
}
