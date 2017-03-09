package com.wifiin.redis;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.ShardedJedis;

public interface RedisConnection extends JedisCommands{
	public ShardedJedis getResource();
	public void returnResource(ShardedJedis jedis);
	public void returnBrokenResource(ShardedJedis jedis);
	public <E> E execute(MethodHandle cmd, String key, Object... args);
	
	public long inc(String key);
	public long dec(String key);
	public Long del(String key);
	public Long del(String... key);
	public long incBy(String key, long value);
	public long decBy(String key, long value);
	/**
	 * 请使用com.wifiin.cache.CacheKeyGenerator
	 * @param parts
	 * @return
	 */
	@Deprecated
	public String generateKey(Object... parts);
	public String getSet(String key);
	public String getString(String key);
	/**
	 * redis值是null或空串，返回null；"0" "false",返回false；其它值，返回true。字符串不分大小写
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key);
	public Long getLong(String key);
	public Integer getInteger(String key);
	public BigDecimal getBigDecimal(String key);
	public Long hgetLong(String key,String field);
	public Integer hgetInteger(String key,String field);
	public BigDecimal hgetBigDecimal(String key,String field);
	public Long lindexLong(String key,long index);
	public Integer lindexInteger(String key,long index);
	public BigDecimal lindexBigDecimal(String key,long index);
	public String get(String key);
	public <E> E get(String key, Class<E> cls);
	public Long set(String key, Map<String,String> value);
	public String set(String key, Object value);
	public String set(String key, Object value,String... fields);
	public String set(String key, String value);
	public Long set(String key, String field, Object value);
	public String setex(String key,int seconds,Object value);
	public Long setnx(String key,Object value);
	public void hsetnx(String key,Object value);
	public Long hsetnx(String key,String field,Object value);
	public Long hset(String key,String field,Object value);
	public Long sadd(String key,Object... values);
	public int compare(String key,Comparable toCompare);
	public int compare(String key, String field, Comparable toCompare);
	public int compare(String key, long index, Comparable toCompare);
	public int hcompare(String key,String field,Comparable toCompare);
	public int compare(String key, Class<Comparable> cls, Comparable toCompare);
	public int del(List keyParts, String... prefix);
	public int del(Object[] keyParts, String... prefix);
	public Long srem(String key, Object... members);
	public String hmset(String key, Object value);
	public Map<String,Object> getMap(String key);
	public Map<String, String> getMap(String key, String... fields);
	public Map<String,Object> getJsonMap(String key);
	public Map<String,Object> getJsonMap(String key, String... fields);
	public <E> E getObjectFromJson(String key, Class<E> cls);
	public String setJsonFromObject(String key, Object value) ;
	public <E> E hmget(String key, Class<E> cls,String... fields) throws Exception;
	public String hmset(String key, Map<String,Object> hash, String... fields);
	public String hmset(String key, Object hash, String... fields);
	public void hsetnx(String key, Object value, String... fields);
	public void hsetnx(String key, Map<String,Object> value);
	public void hsetnx(String key,Map<String,Object> value, String... fields);
	public List<String> lgetAll(String key);
	public Long lpush(String key,Object... value);
	public Long lpushx(String key,String value);
	public Long rpushx(String key,String value);
	public Long lpush(String key,Collection value);
	public String zindex(String key, long index);
	public int zindexInteger(String key, long index);
	public long zindexLong(String key, long index);
	public String zfirst(String key);
	public Long expire(String key, long expire);

	public void delKeys(String keyPattern);
	
	public Object eval(String script,String[] keys, String... args);
	public Object evalsha(String sha,String[] keys, String... args);
	public String scriptLoad(String key,String script);
	public String compareAndSet(String key,String oldVal,String newVal);
	public long incrIfLessThan(String key,long ceil);
	public long incrByIfLessThan(String key,long incr, long ceil);
	public long decrIfLargerThan(String key,long floor);
	public long decrByIfLargerThan(String key, long decr,long floor);
	public long hincrByCompare(String key,String name,long incr,long val);
	public long zincrByCompare(String key,String name,long incr,long val);
}
