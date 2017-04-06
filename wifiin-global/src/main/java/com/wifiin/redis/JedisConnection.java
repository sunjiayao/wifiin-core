package com.wifiin.redis;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.exception.JsonGenerationException;
import com.wifiin.exception.JsonParseException;
import com.wifiin.exception.RedisException;
import com.wifiin.util.Help;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Client;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Pool;

public class JedisConnection implements RedisConnection{
	private static final Logger log=LoggerFactory.getLogger(JedisConnection.class);
	private boolean toThrowOnError;
	private Pool<ShardedJedis> jedisPool;
	public JedisConnection(){}
	public JedisConnection(Pool<ShardedJedis> jedisPool){
	    this.jedisPool=jedisPool;
	}
	public boolean isToThrowOnError() {
		return toThrowOnError;
	}

	public void setToThrowOnError(boolean toThrowOnError) {
		this.toThrowOnError = toThrowOnError;
	}

	public Pool<ShardedJedis> getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(Pool<ShardedJedis> jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public ShardedJedis getResource(){
		return jedisPool.getResource();
	}
	
	@Override
	public void returnResource(ShardedJedis jedis){
		jedisPool.returnResource(jedis);
	}
	
	@Override
	public void returnBrokenResource(ShardedJedis jedis){
		jedisPool.returnBrokenResource(jedis);
	}
	
	@Override
	public <E> E execute(MethodHandle cmd, String key, Object... args){
		ShardedJedis jedis=null;
		boolean released=false;
		try{
			jedis=getResource();
			Object[] params=new Object[2+args.length];
			params[0]=jedis;
			params[1]=key;
			System.arraycopy(args, 0, params, 2, args.length);
			if(log.isDebugEnabled()){
				Client client=jedis.getShard(key).getClient();
				log.debug("JedisConnection.execute:"+client.getHost()+":"+client.getPort()+"/"+cmd+":"+key+":"+java.util.Arrays.toString(args));
			}
			return (E)cmd.invokeWithArguments(params);
		}catch(Throwable e){
			//==========start
			log.error(ThreadLocalStringBuilder.builder().append("JedisConnection.execute:").append(cmd.toString()).append(":").append(key).append(Arrays.toString(args)).append(" ").append(e.toString()).toString(),e);
			returnBrokenResource(jedis);
			released=true;
			jedis=null;
			//==========end   如果出了异常或长时间阻塞就把这部分修改如下
//			jedis.disconnect();//调用jedisPool.returnBrokenResource(jedis)发生阻塞，替换成这一行
			if(this.toThrowOnError){
				throw new RedisException(e);
			}else{
				log.error(ThreadLocalStringBuilder.builder().append("JedisConnection.execute:").append(cmd.toString()).append(":").append(key).append(Arrays.toString(args)).append(" ").append(e.toString()).toString(),e);
				return null;
			}
		}finally{
			if(jedis!=null && !released){
				try{
					returnResource(jedis);
				}catch(Exception e){
					try{
						returnBrokenResource(jedis);
					}catch(Exception e2){
						jedis.close();
					}
				}
			}
		}
	}
	@Deprecated
	@Override
	public String generateKey(Object... parts) {
		return Help.concat(parts, ":");
	}
	
	@Override
	public String get(String key){
		return execute(JedisMethodHandles.get,key);
	}

	@Override
	public <E> E get(String key, Class<E> cls) {
		try {
			Map m=getMap(key);
			if(m.size()==0){
				return null;
			}
			Object r=cls.newInstance();
			for(Map.Entry entry:(Set<Map.Entry>)m.entrySet()){
				String fn=(String)entry.getKey();
				Field f=null;
				try{
					f=cls.getDeclaredField(fn);
					f.setAccessible(true);
					if(Help.isFinalOrStaticField(f)){
						continue;
					}
					String v=(String)entry.getValue();
					if(f.getType()==Date.class){
						f.set(r,new Date(Long.parseLong(v)));
					}else{
						Help.populate(r, fn, v);
					}
				}catch(Exception e){}
			}
			return (E)r;
		} catch (Exception e) {
			throw new RedisException(e);
		}
	}
	@Override
	public String getSet(String key){
		return execute(JedisMethodHandles.getSet,key);
	}
	@Override
	public Map<String, Object> getMap(String key) {
		return execute(JedisMethodHandles.hgetAll,key);
	}

	@Override
	public Map<String, String> getMap(String key, String... fields){
		List<String> list=hmget(key,fields);
		Map<String,String> m=null;
		if(list!=null){
			m=new HashMap<>();
			for(int i=0,l=list.size();i<l;i++){
				String v=list.get(i);
				if(Help.isNotEmpty(v)){
					m.put(fields[i], v);
				}
			}
		}
		return m;
	}

	@Override
	public String getString(String key) {
		return get(key);
	}
	@Override
	public Boolean getBoolean(String key){
		String v=get(key);
		if(Help.isEmpty(v)){
			return null;
		}else if("false".equalsIgnoreCase(v) || RegexUtil.matches(v, "^0?(\\.0*)?$")){
			return false;
		}else{
			return true;
		}
	}
	@Override
	public Long getLong(String key) {
		String v=get(key);
		return Help.isEmpty(v)?null:Long.parseLong(v);
	}

	@Override
	public Integer getInteger(String key) {
		String v=get(key);
		return Help.isEmpty(v)?null:Integer.parseInt(v);
	}
	@Override
	public BigDecimal getBigDecimal(String key){
		String v=get(key);
		return Help.isEmpty(v)?null:new BigDecimal(v);
	}
	@Override
	public Long hgetLong(String key,String field){
		String v=hget(key,field);
		return Help.isEmpty(v)?null:Long.parseLong(v);
	}
	@Override
	public Integer hgetInteger(String key,String field){
		String v=hget(key,field);
		return Help.isEmpty(v)?null:Integer.parseInt(v);
	}
	@Override
	public BigDecimal hgetBigDecimal(String key,String field){
		String v=hget(key,field);
		return Help.isEmpty(v)?null:new BigDecimal(v);
	}
	@Override
	public Long lindexLong(String key,long index){
		String v=lindex(key,index);
		return Help.isEmpty(v)?null:Long.parseLong(v);
	}
	@Override
	public Integer lindexInteger(String key,long index){
		String v=lindex(key,index);
		return Help.isEmpty(v)?null:Integer.parseInt(v);
	}
	@Override
	public BigDecimal lindexBigDecimal(String key,long index){
		String v=lindex(key,index);
		return Help.isEmpty(v)?null:new BigDecimal(v);
	}

	@Override
	public long inc(String key) {
		Long v=incr(key);
		return v==null?0:v;
	}

	@Override
	public long dec(String key) {
		Long v=decr(key);
		return v==null?0:v;
	}

	@Override
	public long incBy(String key, long value) {
		Long v=incrBy(key,value);
		return v==null?0:v;
	}

	@Override
	public long decBy(String key, long value) {
		Long v=decrBy(key,value);
		return v==null?0:v;
	}
	
	@Override
	public String set(String key, String value) {
		return execute(JedisMethodHandles.set,key,value);
	}
	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return execute(JedisMethodHandles.setNXXX_EXPX,key,value,nxxx,expx,time);
	}
	@Override
	public Long set(String key, Map<String, String> value) {
		long r=0L;
		for(Map.Entry<String,String> e:value.entrySet()){
			Object v=e.getValue();
			if(v!=null){
				r&=this.set(key, e.getKey(),(Object)value2String(v));
			}
		}
		return r;
	}
	
	@Override
	public Long set(String key, String field, Object value){
		return hset(key,field,value!=null?value.toString():null);
	}

	@Override
	public String set(String key, Object value) {
		if(value instanceof String || value instanceof Number || value instanceof Boolean){
			return set(key,value.toString());
		}else if(value instanceof Map){
			Map map=(Map)value;
			Map mv=new HashMap();
			for(Map.Entry entry:(Set<Map.Entry>)map.entrySet()){
				Object v=entry.getValue();
				if(v!=null){
					mv.put(entry.getKey(), value2String(v));
				}
			}
			return String.valueOf(set(key,(Map)mv));
		}else{
			Class c=value.getClass();
			Field[] fs=c.getDeclaredFields();
			Map m=new HashMap();
			for(int i=0,l=fs.length;i<l;i++){
				Field f=fs[i];
				f.setAccessible(true);
				if(Help.isFinalOrStaticField(f)){
					continue;
				}
				try {
					Object v=f.get(value);
					if(v!=null){
						m.put(f.getName(), value2String(v));
					}
				} catch (Exception e) {}
			}
			return String.valueOf(set(key,m));
		}
	}
	@Override
	public String set(String key, Object value, String... fields) {
		if(Help.isEmpty(fields)){
			return "0";
		}
		if(value instanceof Map){
			Map map=(Map)value;
			Map mv=new HashMap();
			for(int i=0,l=fields.length;i<l;i++){
				String fn=fields[i];
				Object v=map.get(fn);
				if(v!=null){
					mv.put(fn, value2String(v));
				}
			}
			return Long.toString(set(key,mv));
		}
		Class c=value.getClass();
		Field[] fs=c.getDeclaredFields();
		Map m=new HashMap();
		if(fields!=null){
			for(int i=0,l=fields.length;i<l;i++){
				String fn=fields[i];
				try{
					Field f=c.getDeclaredField(fn);
					f.setAccessible(true);
					if(Help.isFinalOrStaticField(f)){
						continue;
					}
					Object v=f.get(value);
					if(v!=null){
						m.put(f.getName(), value2String(v));
					}
				}catch(Exception e){}
			}
		}
		return Long.toString(set(key,m));
	}

	@Override
	public Long append(String key, String append) {
		return execute(JedisMethodHandles.append,key,append);
	}

	@Override
	public Long decr(String key) {
		return execute(JedisMethodHandles.decr,key);
	}

	@Override
	public Long decrBy(String key, long value) {
		return execute(JedisMethodHandles.decrBy,key,value);
	}

	@Override
	public Boolean exists(String key) {
		return execute(JedisMethodHandles.exists,key);
	}

	@Override
	public Long expire(String key, int expire) {
		return execute(JedisMethodHandles.expire,key,expire);
	}
	@Override
	public Long expire(String key, long expire){
		return expire(key,(int)expire);
	}

	@Override
	public Long expireAt(String key, long expireAt) {
		return execute(JedisMethodHandles.expireAt,key,expireAt);
	}

	@Override
	public String getSet(String key, String value) {
		return execute(JedisMethodHandles.getSet,key,value);
	}

	@Override
	public Boolean getbit(String key, long offset) {
		return execute(JedisMethodHandles.getbit,key,offset);
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return execute(JedisMethodHandles.getrange,key,startOffset,endOffset);
	}

	@Override
	public Long hdel(String key, String... fields) {
		return execute(JedisMethodHandles.hdel,key,fields);
	}

	@Override
	public Boolean hexists(String key, String field) {
		return execute(JedisMethodHandles.hexists,key,field);
	}

	@Override
	public String hget(String key, String field) {
		return execute(JedisMethodHandles.hget,key,field);
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		return execute(JedisMethodHandles.hgetAll,key);
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		return execute(JedisMethodHandles.hincrBy,key,field,value);
	}

	@Override
	public Set<String> hkeys(String key) {
		return execute(JedisMethodHandles.hkeys,key);
	}

	@Override
	public Long hlen(String key) {
		return execute(JedisMethodHandles.hlen,key);
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		return execute(JedisMethodHandles.hmget,key,fields);
	}
	@Override
	public <E> E hmget(String key, Class<E> cls,String... fields) throws Exception{
		List<String> list=hmget(key,fields);
		if(list==null){
			return null;
		}
		Object r=cls.newInstance();
		for(int i=0,l=list.size();i<l;i++){
			String fn=fields[i];
			Field f=null;
			try{
				f=cls.getDeclaredField(fn);
				f.setAccessible(true);
				String v=list.get(i);
				if(Help.isFinalOrStaticField(f) || v==null){
					continue;
				}
				if(f.getType()==Date.class){
					f.set(r,new Date(Long.parseLong(v)));
				}else{
					Help.populate(r, fn, v);
				}
			}catch(Exception e){}
		}
		return (E)r;
	}
	private String hmset0(String key, Map<String,String> hash){
		return execute(JedisMethodHandles.hmset,key,hash);
	}
	@Override
	public String hmset(String key, Map<String,Object> hash, String... fields){
		Map m=new HashMap();
		if(Help.isNotEmpty(fields)){
			for(int i=0,l=fields.length;i<l;i++){
				Object v=hash.get(fields[i]);
				if(v!=null){
					m.put(fields[i],value2String(v));
				}
			}
		}
		return hmset0(key,m);
	}
	@Override
	public String hmset(String key, Object hash, String... fields){
        if(hash instanceof Map){
            return hmset(key,(Map)hash,fields);
        }
        if(Help.isEmpty(fields)){
            return "0";
        }
        Map m=new HashMap();
        if(Help.isNotEmpty(fields)){
            Class c=hash.getClass();
            for(int i=0,l=fields.length;i<l;i++){
                try{
                    String fn=fields[i];
                    Method getter=c.getMethod(ThreadLocalStringBuilder.builder().append("get").append(fn).replace(3,4,String.valueOf(Character.toUpperCase(fn.charAt(0)))).toString());
                    Object v=getter.invoke(hash);
                    if(v!=null){
                        m.put(fields[i],value2String(v));
                    }
                }catch(Exception e){}
            }
        }
        return hmset0(key,m);
    }
	
	@Override
	public String hmset(String key, Map<String, String> hash) {
		Map mv=new HashMap();
		for(Map.Entry e:hash.entrySet()){
			Object v=e.getValue();
			if(v!=null){
				mv.put(e.getKey(), value2String(v));
			}
		}
		return hmset0(key,mv);
	}
	@SuppressWarnings({"unchecked","rawtypes"})
    @Override
	public String hmset(String key, Object value){
		if(value instanceof Map){
			return hmset(key,(Map)value);
		}else{
			Class c=value.getClass();
			Method[] methods=c.getMethods();
			Map m=new HashMap();
			for(int i=0,l=methods.length;i<l;i++){
				Method method=methods[i];
				try {
					Object v=method.invoke(value);
					if(v!=null){
					    String mn=method.getName();
					    mn=mn.substring(3);
					    mn=ThreadLocalStringBuilder.builder().append(mn).replace(0,1,String.valueOf(Character.toLowerCase(mn.charAt(0)))).toString();
						m.put(mn, value2String(v));
					}
				} catch (Exception e) {}
			}
			return hmset0(key,m);
		}
	}

	@Override
	public Long hset(String key, String field, String value) {
		return execute(JedisMethodHandles.hset,key,field,value);
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		return execute(JedisMethodHandles.hsetnx,key,field,value);
	}

	@Override
	public List<String> hvals(String key) {
		return execute(JedisMethodHandles.hvals,key);
	}

	@Override
	public Long incr(String key) {
		return execute(JedisMethodHandles.incr,key);
	}

	@Override
	public Long incrBy(String key, long value) {
		return execute(JedisMethodHandles.incrBy,key,value);
	}

	@Override
	public String lindex(String key, long index) {
		return execute(JedisMethodHandles.lindex,key,index);
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		return execute(JedisMethodHandles.lindex,key,where,pivot,value);
	}

	@Override
	public Long llen(String key) {
		return execute(JedisMethodHandles.llen,key);
	}

	@Override
	public String lpop(String key) {
		return execute(JedisMethodHandles.lpop,key);
	}

	@Override
	public Long lpush(String key, String... values) {
		return execute(JedisMethodHandles.lpush,key,values);
	}
	@Override
	public Long lpush(String key,Object... value){
		String[] vs=new String[value.length];
		for(int i=0,l=value.length;i<l;i++){
			vs[i]=value[i].toString();
		}
		return lpush(key,vs);
	}
	@Override
	public Long lpush(String key,Collection value){
		return lpush(key,value.toArray());
	}

	@Override
	public Long lpushx(String key, String value) {
		return execute(JedisMethodHandles.lpushx,key,value);
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		return execute(JedisMethodHandles.lrange,key,start,end);
	}
	@Override
	public List<String> lgetAll(String key){
		return lrange(key,0,this.llen(key));
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return execute(JedisMethodHandles.lrem,key,count,value);
	}

	@Override
	public String lset(String key, long index, String value) {
		return execute(JedisMethodHandles.lset,key,index,value);
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return execute(JedisMethodHandles.ltrim,key,start,end);
	}

	@Override
	public String rpop(String key) {
		return execute(JedisMethodHandles.rpop,key);
	}

	@Override
	public Long rpush(String key, String... values) {
		return execute(JedisMethodHandles.rpush,key,values);
	}

	@Override
	public Long rpushx(String key, String value) {
		return execute(JedisMethodHandles.rpushx,key,value);
	}

	@Override
	public Long sadd(String key, String... members) {
		return execute(JedisMethodHandles.sadd,key,members);
	}
	
	@Override
	public Long sadd(String key, Object... members){
		if(members==null){
			sadd(key,null);
		}
		String[] ms=new String[members.length];
		for(int i=0,l=members.length;i<l;i++){
			ms[i]=members[i].toString();
		}
		return sadd(key,ms);
	}

	@Override
	public Long scard(String key) {
		return execute(JedisMethodHandles.scard,key);
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		return execute(JedisMethodHandles.setbit,key,offset,value);
	}

	@Override
	public String setex(String key, int seconds, String value) {
		return execute(JedisMethodHandles.setex,key,seconds,value);
	}

	@Override
	public Long setnx(String key, String value) {
		return execute(JedisMethodHandles.setnx,key,value);
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		return execute(JedisMethodHandles.setrange,key,offset,value);
	}

	@Override
	public Boolean sismember(String key, String member) {
		return execute(JedisMethodHandles.sismember,key,member);
	}

	@Override
	public Set<String> smembers(String key) {
		return execute(JedisMethodHandles.smembers,key);
	}

	@Override
	public List<String> sort(String key) {
		return execute(JedisMethodHandles.sort,key);
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParams) {
		return execute(JedisMethodHandles.sortWITH_PARAMS,key,sortingParams);
	}

	@Override
	public String spop(String key) {
		return execute(JedisMethodHandles.spop,key);
	}

	@Override
	public String srandmember(String key) {
		return execute(JedisMethodHandles.srandmember,key);
	}

	@Override
	public Long srem(String key, String... members) {
		return execute(JedisMethodHandles.srem,key,members);
	}

	@Override
	public String substr(String key, int start, int end) {
		return execute(JedisMethodHandles.substr,key,start,end);
	}

	@Override
	public Long ttl(String key) {
		return execute(JedisMethodHandles.ttl,key);
	}

	@Override
	public String type(String key) {
		return execute(JedisMethodHandles.type,key);
	}

//	@Override
//	public Long zadd(String key, Map<String, Double> scoreMembers) {
//		return execute(JedisMethodHandles.zaddMULTI,key);
//	}
	@Override
	public Long zadd(String key, Map<String, Double> members) {
		return execute(JedisMethodHandles.zaddMULTI,key);
	}
	@Override
	public Long zadd(String key, double score, String member) {
		return execute(JedisMethodHandles.zadd,key,score,member);
	}

	@Override
	public Long zcard(String key) {
		return execute(JedisMethodHandles.zcard,key);
	}

	@Override
	public Long zcount(String key, double min, double max) {
		return execute(JedisMethodHandles.zcountDOUBLE,key,min,max);
	}

	@Override
	public Long zcount(String key, String min, String max) {
		return execute(JedisMethodHandles.zcountSTRING,key,min,max);
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		return execute(JedisMethodHandles.zincrby,key,score,member);
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		return execute(JedisMethodHandles.zrange,key,start,end);
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		return execute(JedisMethodHandles.zrangeByScoreDOUBLE_MINMAX,key,min,max);
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return execute(JedisMethodHandles.zrangeByScoreSTRING_MINMAX,key,min,max);
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return execute(JedisMethodHandles.zrangeByScoreDOUBLE_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return execute(JedisMethodHandles.zrangeByScoreSTRING_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min,double max) {
		return execute(JedisMethodHandles.zrangeByScoreWithScoresDOUBLE_MINMAX,key,min,max);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return execute(JedisMethodHandles.zrangeByScoreWithScoresSTRING_MINMAX,key,min,max);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return execute(JedisMethodHandles.zrangeByScoreWithScoresDOUBLE_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return execute(JedisMethodHandles.zrangeByScoreWithScoresSTRING_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return execute(JedisMethodHandles.zrangeWithScores,key,start,end);
	}

	@Override
	public Long zrank(String key, String member) {
		return execute(JedisMethodHandles.zrank,key,member);
	}

	@Override
	public Long zrem(String key, String... members) {
		return execute(JedisMethodHandles.zrem,key,members);
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		return execute(JedisMethodHandles.zremrangeByRank,key,start,end);
	}

	@Override
	public Long zremrangeByScore(String key, double min, double max) {
		return execute(JedisMethodHandles.zremrangeByScoreDOUBLE_MINMAX,key,min,max);
	}

	@Override
	public Long zremrangeByScore(String key, String min, String max) {
		return execute(JedisMethodHandles.zremrangeByScoreSTRING_MINMAX,key,min,max);
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		return execute(JedisMethodHandles.zrevrange,key,start,end);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double min, double max) {
		return execute(JedisMethodHandles.zrevrangeByScoreDOUBLE_MINMAX,key,min,max);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String min, String max) {
		return execute(JedisMethodHandles.zrevrangeByScoreSTRING_MINMAX,key,min,max);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double min, double max, int offset, int count) {
		return execute(JedisMethodHandles.zrevrangeByScoreDOUBLE_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String min, String max, int offset, int count) {
		return execute(JedisMethodHandles.zrevrangeByScoreSTRING_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double min, double max) {
		return execute(JedisMethodHandles.zrevrangeByScoreWithScoresDOUBLE_MINMAX,key,min,max);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String min, String max) {
		return execute(JedisMethodHandles.zrevrangeByScoreWithScoresSTRING_MINMAX,key,min,max);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return execute(JedisMethodHandles.zrevrangeByScoreWithScoresDOUBLE_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return execute(JedisMethodHandles.zrevrangeByScoreWithScoresSTRING_MINMAX_OFFSET_COUNT,key,min,max,offset,count);
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return execute(JedisMethodHandles.zrevrangeWithScores,key,start,end);
	}

	@Override
	public Long zrevrank(String key, String member) {
		return execute(JedisMethodHandles.zrevrank,key,member);
	}

	@Override
	public Double zscore(String key, String member) {
		return execute(JedisMethodHandles.zscore,key,member);
	}

	@Override
	public String setex(String key,int seconds, Object value) {
		if(value instanceof String || value instanceof Number || value instanceof Boolean){
			return setex(key,seconds,value.toString());
		}else if(value instanceof Map){
			Map map=(Map)value;
			for(Map.Entry entry:(Set<Map.Entry>)map.entrySet()){
				entry.setValue(entry.getValue().toString());
			}
			String code=hmset(key,(Map)value);
			expire(key, seconds);
			return code;
		}else{
			Class c=value.getClass();
			Field[] fs=c.getDeclaredFields();
			Map m=new HashMap();
			for(int i=0,l=fs.length;i<l;i++){
				Field f=fs[i];
				f.setAccessible(true);
				if(Help.isFinalOrStaticField(f)){
					continue;
				}
				try {
					m.put(f.getName(), value2String(f.get(value)));
				} catch (Exception e) {}
			}
			String code=hmset(key,m);
			expire(key,seconds);
			return code;
		}
	}

	@Override
	public Long setnx(String key, Object value) {
		if(value instanceof String || value instanceof Number || value instanceof Boolean){
			return setnx(key,value.toString());
		}else if(value instanceof Map){
			Map map=(Map)value;
			Long code=0L;
			for(Map.Entry entry:(Set<Map.Entry>)map.entrySet()){
				code=hsetnx(key,entry.getKey().toString(),entry.getValue().toString());
			}
			return code;
		}else{
			Class c=value.getClass();
			Field[] fs=c.getDeclaredFields();
			Long code=0L;
			for(int i=0,l=fs.length;i<l;i++){
				Field f=fs[i];
				f.setAccessible(true);
				if(Help.isFinalOrStaticField(f)){
					continue;
				}
				try {
					code=hsetnx(key,f.getName(),value2String(f.get(value)));
				} catch (Exception e) {}
			}
			return code;
		}
	}
	private int compareTo(String v,Comparable toCompare){
		if(v==null && toCompare==null){
			return 0;
		}else if(v!=null && toCompare==null){
			return 1;
		}else if(v==null && toCompare!=null){
			return -1;
		}
		if(toCompare instanceof String){
			return v.compareTo((String)toCompare);
		}else if(toCompare instanceof Long){
			return Long.valueOf(v).compareTo((Long)toCompare);
		}else if(toCompare instanceof Integer){
			return Integer.valueOf(v).compareTo((Integer)toCompare);
		}else if(toCompare instanceof Short){
			return Short.valueOf(v).compareTo((Short)toCompare);
		}else if(toCompare instanceof Byte){
			return Byte.valueOf(v).compareTo((Byte)toCompare);
		}else if(toCompare instanceof Boolean){
			return Boolean.valueOf(v).compareTo((Boolean)toCompare);
		}else if(toCompare instanceof Character){
			return v.charAt(0)-((Character)toCompare).charValue();
		}else if(toCompare instanceof BigDecimal){
			return new BigDecimal(v).compareTo((BigDecimal)toCompare);
		}else if(toCompare instanceof BigInteger){
			return new BigInteger(v).compareTo((BigInteger)toCompare);
		}else if(toCompare instanceof Double){
			return new Double(v).compareTo((Double)toCompare);
		}else if(toCompare instanceof Float){
			return new Float(v).compareTo((Float)toCompare);
		}else{
			return v.compareTo(toCompare.toString());
		}
	}
	@Override
	public int compare(String key, String field, Comparable toCompare){
		return compareTo(hget(key,field),toCompare);
	}
	@Override
	public int compare(String key, long index, Comparable toCompare){
		return compareTo(lindex(key,index),toCompare);
	}
	@Override
	public int compare(String key, Comparable toCompare) {
		return compareTo(get(key),toCompare);
	}

	@Override
	public int hcompare(String key, String field, Comparable toCompare) {
		return compareTo(hget(key,field),toCompare);
	}

	@Override
	public int compare(String key, Class<Comparable> cls, Comparable toCompare) {
		Comparable v=get(key,cls);
		if(v==null && toCompare==null){
			return 0;
		}else if(v!=null && toCompare==null){
			return 1;
		}else if(v==null && toCompare!=null){
			return -1;
		}else{
			return get(key,cls).compareTo(toCompare);
		}
	}

	@Override
	public Long del(String key){
		return this.execute(JedisMethodHandles.del, key);
	}
	@Override
	public Long del(String... key) {
		int c=0;
		for(int i=0,l=key.length;i<l;i++){
			c+=del(key[i]).intValue();
		}
		return (long)c;
	}
	@Override
	public int del(List keyParts, String... prefix){
		String pre=this.generateKey(prefix);
		int c=0;
		for(int i=0,l=keyParts.size();i<l;i++){
			c+=del(this.generateKey(pre,keyParts.get(i))).intValue();
		}
		return c;
	}
	@Override
	public int del(Object[] keyParts, String... prefix){
		String pre=this.generateKey(prefix);
		int c=0;
		for(int i=0,l=keyParts.length;i<l;i++){
			c+=del(this.generateKey(pre,keyParts[i])).intValue();
		}
		return c;
	}

	@Override
	public void hsetnx(String key, Object value) {
		Class c=value.getClass();
		Field[] fs=c.getDeclaredFields();
		for(int i=0,l=fs.length;i<l;i++){
			Field f=fs[i];
			f.setAccessible(true);
			if(Help.isFinalOrStaticField(f)){
				continue;
			}
			try {
				hsetnx(key,f.getName(),value2String(f.get(value)));
			} catch (Exception e) {}
		}
	}
	@Override
	public void hsetnx(String key, Map<String,Object> value){
		for(Map.Entry<String,Object> e:value.entrySet()){
			hsetnx(e.getKey(),value2String(e.getValue()));
		}
	}
	@Override
	public void hsetnx(String key,Map<String,Object> value, String... fields){
		for(int i=0,l=fields.length;i<l;i++){
			hsetnx(key,fields[i],value2String(value.get(fields[i])));
		}
	}
	@Override
	public void hsetnx(String key, Object value, String... fields) {
		if(Help.isNotEmpty(fields)){
			Class c=value.getClass();
			for(int i=0,l=fields.length;i<l;i++){
				try {
					Field f=c.getDeclaredField(fields[i]);
					f.setAccessible(true);
					if(Help.isFinalOrStaticField(f)){
						continue;
					}
					hsetnx(key,f.getName(),value2String(f.get(value)));
				} catch (Exception e) {}
			}
		}
	}

	@Override
	public Long hsetnx(String key, String field, Object value) {
		return this.hsetnx(key, field, value2String(value));
	}

	@Override
	public Long hset(String key, String field, Object value) {
		return this.hset(key, field, value2String(value));
	}
	@Override
	public Long srem(String key, Object... members) {
		String[] ms=new String[members.length];
		for(int i=0,l=members.length;i<l;i++){
			ms[i]=members[i].toString();
		}
		return srem(key,ms);
	}
	private String value2String(Object v){
		if(v==null){
			return null;
		}
		return v instanceof Date?Long.toString(((Date)v).getTime()):v.toString();
	}
	

	@Override
	public String zindex(String key, long index) {
		Set<String> s=zrange(key,index,index);
		return s!=null?null:s.iterator().next();
	}

	@Override
	public int zindexInteger(String key, long index) {
		String v=zindex(key,index);
		return v!=null?Integer.parseInt(v):null;
	}

	@Override
	public long zindexLong(String key, long index) {
		String v=zindex(key,index);
		return v!=null?Long.parseLong(v):null;
	}

	@Override
	public String zfirst(String key) {
		return zindex(key,0);
	}

	@Override
	public Map<String, Object> getJsonMap(String key){
		return getObjectFromJson(key,Map.class);
	}

	@Override
	public Map<String, Object> getJsonMap(String key, String... fields){
		Map map=getJsonMap(key);
		if(map==null){
			return null;
		}else if(Help.isEmpty(fields)){
			return new HashMap<String,Object>();
		}else{
			Map<String,Object> result=new HashMap<>();
			for(int i=0,l=fields.length;i<l;i++){
				String k=fields[i];
				result.put(k, map.get(k));
			}
			return result;
		}
	}

	@Override
	public <E> E getObjectFromJson(String key, Class<E> cls){
		String value=get(key);
		if(Help.isEmpty(value)){
			return null;
		}
		try{
            return value==null?null:GlobalObject.getJsonMapper().readValue(value, cls);
        }catch(IOException e){
            throw new JsonParseException(e);
        }
	}
	@Override
	public String setJsonFromObject(String key, Object value) {
		if(value==null){
			String nullString=null;
			return set(key,nullString);
		}else{
			try{
                return set(key,GlobalObject.getJsonMapper().writeValueAsString(value));
            }catch(JsonProcessingException e){
                throw new JsonGenerationException(e);
            }
		}
	}
	@Override
	public String setJsonFromObjectExpire(String key,Object value,int expire){
	    if(value==null){
            String nullString=null;
            return set(key,nullString);
        }else{
            try{
                return setex(key,expire,GlobalObject.getJsonMapper().writeValueAsString(value));
            }catch(JsonProcessingException e){
                throw new JsonGenerationException(e);
            }
        }
	}
	@Override
    public String setJsonFromObjectExpireAt(String key,Object value,long expireAt){
	    if(value==null){
            String nullString=null;
            return set(key,nullString);
        }else{
            try{
                String result = set(key,GlobalObject.getJsonMapper().writeValueAsString(value));
                expireAt(key,expireAt);
                return result;
            }catch(JsonProcessingException e){
                throw new JsonGenerationException(e);
            }
        }
	}
	@Override
	public void delKeys(String keyPattern){
	    ShardedJedis sharded=jedisPool.getResource();
	    boolean ex=false;
	    try{
    		for(Jedis j:sharded.getAllShards()){
    		    try{
    		        for(String k:j.keys(keyPattern)){
    		            j.del(k);
    		        }
    		    }catch(Exception e){
    		        ex=true;
    		    }
    		}
	    }finally{
	        sharded.close();
	    }
	}

//	@Override
//	public Long persist(String key) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Boolean setbit(String key, long offset, String value) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long strlen(String key) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long lpushx(String key, String... string) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long rpushx(String key, String... string) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> blpop(String arg) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> brpop(String arg) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String echo(String string) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long move(String key, int dbIndex) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long bitcount(String key) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Long bitcount(String key, long start, long end) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<String> sscan(String key, int cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<Tuple> zscan(String key, int cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<String> sscan(String key, String cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ScanResult<Tuple> zscan(String key, String cursor) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	@Override
	public Long bitcount(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitcount(String arg0, long arg1, long arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> blpop(String key) {
		return execute(JedisMethodHandles.blpop,key);
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		ShardedJedis jedis=null;
		boolean released=false;
		try{
			jedis=getResource();
			return (List<String>)jedis.blpop(timeout,key);
		}catch(Throwable e){
			//==========start
			log.error(ThreadLocalStringBuilder.builder().append("JedisConnection.blpop:").append(key).append(",").append(timeout).append(" ").append(e.toString()).toString(),e);
			returnBrokenResource(jedis);
			released=true;
			jedis=null;
			//==========end   如果出了异常或长时间阻塞就把这部分修改如下
//			jedis.disconnect();//调用jedisPool.returnBrokenResource(jedis)发生阻塞，替换成这一行
			if(this.toThrowOnError){
				throw new RedisException(e);
			}else{
				return null;
			}
		}finally{
			if(jedis!=null && !released){
				try{
					returnResource(jedis);
				}catch(Exception e){
					try{
						returnBrokenResource(jedis);
					}catch(Exception e2){
						jedis.close();
					}
				}
			}
		}
	}

	@Override
	public List<String> brpop(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> brpop(int arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String echo(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long lpushx(String arg0, String... arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long move(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long persist(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pfadd(String arg0, String... arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long pfcount(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long rpushx(String arg0, String... arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setbit(String arg0, long arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> srandmember(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> sscan(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> sscan(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long strlen(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zlexcount(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrangeByLex(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrangeByLex(String arg0, String arg1, String arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zremrangeByLex(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double incrByFloat(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pexpire(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pexpireAt(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> spop(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrevrangeByLex(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrevrangeByLex(String arg0, String arg1, String arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		return null;
	}
	private Object eval(Jedis jedis,boolean sha,String script,int numbers,String... args){
		if(sha){
			if(numbers==0){
				if(Help.isEmpty(args)){
					return jedis.evalsha(script);
				}
			}
			return jedis.evalsha(script,numbers,args);
		}else{
			if(numbers==0){
				if(Help.isEmpty(args)){
					return jedis.eval(script);
				}
			}
			return jedis.eval(script,numbers,args);
		}
	}
	private Object eval(boolean sha,String script, String[] keys, String... args){
		ShardedJedis sharded=null;
		boolean released=false;
		try{
			sharded=jedisPool.getResource();
			Jedis jedis=sharded.getShard(keys[0]);
			Object result=null;
			if(Help.isEmpty(keys)){
				result=eval(jedis,sha,script,0,args);
			}else{
				int l=0;
				if(args==null){
					l=keys.length+args.length;
				}else{
					l=keys.length;
				}
				String[] params=new String[l];
				System.arraycopy(keys, 0, params, 0, keys.length);
				System.arraycopy(args, 0, params, keys.length, args.length);
				result=eval(jedis,sha,script,keys.length,params);
			}
			return result;
		}catch(Throwable e){
			//==========start
			log.error(ThreadLocalStringBuilder.builder().append("JedisConnection.eval:").append(sha).append(" ").append(e.toString()).toString(),e);
			returnBrokenResource(sharded);
			released=true;
			sharded=null;
			//==========end   如果出了异常或长时间阻塞就把这部分修改如下
//			jedis.disconnect();//调用jedisPool.returnBrokenResource(jedis)发生阻塞，替换成这一行
			if(this.toThrowOnError){
				throw new RedisException(e);
			}else{
				return null;
			}
		}finally{
			if(sharded!=null && !released){
				try{
					returnResource(sharded);
				}catch(Exception e){
					try{
						returnBrokenResource(sharded);
					}catch(Exception e2){
						sharded.close();
					}
				}
			}
		}
	}
	@Override
	public Object eval(String script, String[] keys, String... args) {
		return eval(false,script,keys,args);
	}

	@Override
	public Object evalsha(String sha, String[] keys, String... args) {
		return eval(true,sha,keys,args);
	}

	@Override
	public String scriptLoad(String key, String script) {
		ShardedJedis sharded=null;
		boolean released=false;
		try{
			sharded=jedisPool.getResource();
			Jedis jedis=sharded.getShard(key);
			String sha=jedis.scriptLoad(script);
			jedisPool.returnResource(sharded);
			return sha;
		}catch(Throwable e){
			//==========start
			log.error(ThreadLocalStringBuilder.builder().append("JedisConnection.scriptLoad:").append(key).append(" ").append(e.toString()).toString(),e);
			returnBrokenResource(sharded);
			released=true;
			sharded=null;
			//==========end   如果出了异常或长时间阻塞就把这部分修改如下
//			jedis.disconnect();//调用jedisPool.returnBrokenResource(jedis)发生阻塞，替换成这一行
			if(this.toThrowOnError){
				throw new RedisException(e);
			}else{
				return null;
			}
		}finally{
			if(sharded!=null && !released){
				try{
					returnResource(sharded);
				}catch(Exception e){
					try{
						returnBrokenResource(sharded);
					}catch(Exception e2){
						sharded.close();
					}
				}
			}
		}
	}
	
	private String compareAndSetSha;
	private String loadCompareAndSet(String key){
		if(compareAndSetSha==null){
			synchronized(this){
				if(compareAndSetSha==null){
					String script="local v=redis.call('get',KEYS[1])\r\n"
							    + "if (~v or v==ARGV[1]) then \r\n"
							    + "    return redis.call('set',KEYS[1],ARGV[2]) \r\n"
							    + "end \r\n"
							    + "return nil";
					compareAndSetSha=scriptLoad(key,script);
				}
			}
		}
		return compareAndSetSha;
	}
	@Override
	public String compareAndSet(String key, String oldVal, String newVal) {
		return (String)evalsha(loadCompareAndSet(key),new String[]{key},oldVal,newVal);
	}
	private String incrIfLessThanSha;
	private String loadIncrIfLessThan(String key){
		if(incrIfLessThanSha==null){
			synchronized(this){
				if(incrIfLessThanSha==null){
					String script="local v=redis.call('get',KEYS[1])\r\n"
							    + "if (~v or v<ARGV[1]) then \r\n"
							    + "    return redis.call('incr',KEYS[1],'1') \r\n"
							    + "end \r\n"
							    + "return v";
					incrIfLessThanSha=scriptLoad(key,script);
				}
			}
		}
		return incrIfLessThanSha;
	}
	@Override
	public long incrIfLessThan(String key, long ceil) {
		return (Long)evalsha(loadIncrIfLessThan(key),new String[]{key},Long.toString(ceil));
	}
	private String incrByIfLessThanSha;
	private String loadIncrByIfLessThan(String key){
		if(incrByIfLessThanSha==null){
			synchronized(this){
				if(incrByIfLessThanSha==null){
					String script="local v=redis.call('get',KEYS[1])\r\n"
							    + "if (~v or v<ARGV[2]) then \r\n"
							    + "    return redis.call('incrby',KEYS[1],ARGV[1]) \r\n"
							    + "end \r\n"
							    + "return v";
					incrByIfLessThanSha=scriptLoad(key,script);
				}
			}
		}
		return incrByIfLessThanSha;
	}
	@Override
	public long incrByIfLessThan(String key, long incr, long ceil) {
		return (Long)evalsha(loadIncrByIfLessThan(key),new String[]{key},Long.toString(incr),Long.toString(ceil));
	}
	private String decrIfLargerThanSha;
	private String loadDecrIfLargerThan(String key){
		if(decrIfLargerThanSha==null){
			synchronized(this){
				if(decrIfLargerThanSha==null){
					String script="local v=redis.call('get',KEYS[1])\r\n"
							    + "if (~v or v>ARGV[1]) then \r\n"
							    + "    return redis.call('decr',KEYS[1],'1') \r\n"
							    + "end \r\n"
							    + "return v";
					decrIfLargerThanSha=scriptLoad(key,script);
				}
			}
		}
		return decrIfLargerThanSha;
	}
	@Override
	public long decrIfLargerThan(String key, long floor) {
		return (Long)evalsha(loadDecrIfLargerThan(key),new String[]{key},Long.toString(floor));
	}
	private String decrByIfLargerThanSha;
	private String loadDecrByIfLargerThan(String key){
		if(decrByIfLargerThanSha==null){
			synchronized(this){
				if(decrByIfLargerThanSha==null){
					String script="local v=redis.call('get',KEYS[1])\r\n"
							    + "if (~v or v>ARGV[2]) then \r\n"
							    + "    return redis.call('decrby',KEYS[1],ARGV[1]) \r\n"
							    + "end \r\n"
							    + "return v";
					decrByIfLargerThanSha=scriptLoad(key,script);
				}
			}
		}
		return decrByIfLargerThanSha;
	}
	@Override
	public long decrByIfLargerThan(String key, long decr, long floor) {
		return (Long)evalsha(loadDecrByIfLargerThan(key),new String[]{key},Long.toString(decr),Long.toString(floor));
	}
	private String hincrByCompare;
	private String loadHincrByCompare(String key){
		if(hincrByCompare==null){
			synchronized(this){
				if(hincrByCompare==null){
					String script="local v=redis.call('hget',KEYS[1],ARGV[1])\r\n"
							    + "if (~v) then \r\n"
							    + "    v=0 \r\n"
							    + "end"
							    + "if ((ARGV[2]<0 and v<ARGV[3]) or (ARGV[2]>0 and v>ARGV[3])) then \r\n"
							    + "    return redis.call('hincrby',KEYS[1],ARGV[1],ARGV[2]) \r\n"
							    + "end \r\n"
							    + "return v";
					hincrByCompare=scriptLoad(key,script);
				}
			}
		}
		return hincrByCompare;
	}
	@Override
	public long hincrByCompare(String key, String name, long incr, long val) {
		return (Long)evalsha(loadHincrByCompare(key),new String[]{key},name,Long.toString(incr),Long.toString(val));
	}
	
	@Override
	public long zincrByCompare(String key, String name, long incr, long val) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public Long bitpos(String arg0,boolean arg1){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long bitpos(String arg0,boolean arg1,BitPosParams arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long geoadd(String arg0,Map<String,GeoCoordinate> arg1){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long geoadd(String arg0,double arg1,double arg2,String arg3){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double geodist(String arg0,String arg1,String arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double geodist(String arg0,String arg1,String arg2,GeoUnit arg3){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> geohash(String arg0,String... arg1){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoCoordinate> geopos(String arg0,String... arg1){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String arg0,double arg1,double arg2,double arg3,GeoUnit arg4){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String arg0,double arg1,double arg2,double arg3,GeoUnit arg4,
            GeoRadiusParam arg5){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String arg0,String arg1,double arg2,GeoUnit arg3){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String arg0,String arg1,double arg2,GeoUnit arg3,
            GeoRadiusParam arg4){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double hincrByFloat(String arg0,String arg1,double arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String arg0,String arg1,ScanParams arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String psetex(String arg0,long arg1,String arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long pttl(String arg0){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String set(String arg0,String arg1,String arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<String> sscan(String arg0,String arg1,ScanParams arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String arg0,Map<String,Double> arg1,ZAddParams arg2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String arg0,double arg1,String arg2,ZAddParams arg3){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double zincrby(String arg0,double arg1,String arg2,ZIncrByParams arg3){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String arg0,String arg1,ScanParams arg2){
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public List<Long> bitfield(String key,String... arguments){
        // TODO Auto-generated method stub
        return null;
    }

}
