package com.wifiin.pool.redis;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.exception.JsonGenerationException;
import com.wifiin.exception.JsonParseException;
import com.wifiin.exception.RedisException;
import com.wifiin.pool.WatchableConfigManagedKeyedPool;
import com.wifiin.redis.RedisConnection;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
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

public class WatchableConfigManagedShardedRedisPool implements RedisConnection{
    public static final String SHARDED_REDIS_POOL_CONFIG="sharded.redis.pool.config";
    private WatchableConfigManagedKeyedPool<RedisPoolKey,Jedis> pool;
    public WatchableConfigManagedShardedRedisPool(){
        pool=new WatchableConfigManagedKeyedPool<>(SHARDED_REDIS_POOL_CONFIG);
    }
    @Override
    public String set(String key,String value,String nxxx,String expx,long time){
        return pool.execute(key,(k,jedis)->{
            return jedis.set(k,value,nxxx,expx,time);
        });
    }

    @Override
    public String set(String key,String value,String nxxx){
        return pool.execute(key,(k,jedis)->{
            return jedis.set(k,value,nxxx);
        });
    }

    @Override
    public Boolean exists(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.exists(k);
        });
    }

    @Override
    public Long persist(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.persist(k);
        });
    }

    @Override
    public String type(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.type(k);
        });
    }

    @Override
    public Long expire(String key,int seconds){
        return pool.execute(key,(k,jedis)->{
            return jedis.expire(k,seconds);
        });
    }

    @Override
    public Long pexpire(String key,long milliseconds){
        return pool.execute(key,(k,jedis)->{
            return jedis.pexpire(k,milliseconds);
        });
    }

    @Override
    public Long expireAt(String key,long unixTime){
        return pool.execute(key,(k,jedis)->{
            return jedis.expireAt(k,unixTime);
        });
    }

    @Override
    public Long pexpireAt(String key,long millisecondsTimestamp){
        return pool.execute(key,(k,jedis)->{
            return jedis.pexpireAt(k,millisecondsTimestamp);
        });
    }

    @Override
    public Long ttl(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.ttl(k);
        });
    }

    @Override
    public Long pttl(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.pttl(k);
        });
    }

    @Override
    public Boolean setbit(String key,long offset,boolean value){
        return pool.execute(key,(k,jedis)->{
            return jedis.setbit(k,offset,value);
        });
    }

    @Override
    public Boolean setbit(String key,long offset,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.setbit(k,offset,value);
        });
    }

    @Override
    public Boolean getbit(String key,long offset){
        return pool.execute(key,(k,jedis)->{
            return jedis.getbit(k,offset);
        });
    }

    @Override
    public Long setrange(String key,long offset,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.setrange(k,offset,value);
        });
    }

    @Override
    public String getrange(String key,long startOffset,long endOffset){
        return pool.execute(key,(k,jedis)->{
            return jedis.getrange(k,startOffset,endOffset);
        });
    }

    @Override
    public String getSet(String key,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.getSet(k,value);
        });
    }

    @Override
    public Long setnx(String key,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.setnx(k,value);
        });
    }

    @Override
    public String setex(String key,int seconds,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.setex(k,seconds,value);
        });
    }

    @Override
    public String psetex(String key,long milliseconds,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.psetex(k,milliseconds,value);
        });
    }

    @Override
    public Long decrBy(String key,long integer){
        return pool.execute(key,(k,jedis)->{
            return jedis.decrBy(k,integer);
        });
    }

    @Override
    public Long decr(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.decr(k);
        });
    }

    @Override
    public Long incrBy(String key,long integer){
        return pool.execute(key,(k,jedis)->{
            return jedis.incrBy(k,integer);
        });
    }

    @Override
    public Double incrByFloat(String key,double value){
        return pool.execute(key,(k,jedis)->{
            return jedis.incrByFloat(k,value);
        });
    }

    @Override
    public Long incr(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.incr(k);
        });
    }

    @Override
    public Long append(String key,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.append(k,value);
        });
    }

    @Override
    public String substr(String key,int start,int end){
        return pool.execute(key,(k,jedis)->{
            return jedis.substr(k,start,end);
        });
    }

    @Override
    public Long hset(String key,String field,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.hset(k,field,value);
        });
    }

    @Override
    public String hget(String key,String field){
        return pool.execute(key,(k,jedis)->{
            return jedis.hget(k,field);
        });
    }

    @Override
    public Long hsetnx(String key,String field,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.hsetnx(k,field,value);
        });
    }

    @Override
    public String hmset(String key,Map<String,String> hash){
        Map mv=new HashMap();
        for(Map.Entry e:hash.entrySet()){
            Object v=e.getValue();
            if(v!=null){
                mv.put(e.getKey(), value2String(v));
            }
        }
        return pool.execute(key,(k,jedis)->{
            return jedis.hmset(k,mv);
        });
    }

    @Override
    public List<String> hmget(String key,String... fields){
        return pool.execute(key,(k,jedis)->{
            return jedis.hmget(k,fields);
        });
    }

    @Override
    public Long hincrBy(String key,String field,long value){
        return pool.execute(key,(k,jedis)->{
            return jedis.hincrBy(k,field,value);
        });
    }

    @Override
    public Double hincrByFloat(String key,String field,double value){
        return pool.execute(key,(k,jedis)->{
            return jedis.hincrByFloat(k,field,value);
        });
    }

    @Override
    public Boolean hexists(String key,String field){
        return pool.execute(key,(k,jedis)->{
            return jedis.hexists(k,field);
        });
    }

    @Override
    public Long hdel(String key,String... field){
        return pool.execute(key,(k,jedis)->{
            return jedis.hdel(k,field);
        });
    }

    @Override
    public Long hlen(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.hlen(k);
        });
    }

    @Override
    public Set<String> hkeys(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.hkeys(k);
        });
    }

    @Override
    public List<String> hvals(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.hvals(k);
        });
    }

    @Override
    public Map<String,String> hgetAll(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.hgetAll(k);
        });
    }

    @Override
    public Long rpush(String key,String... string){
        return pool.execute(key,(k,jedis)->{
            return jedis.rpush(k,string);
        });
    }

    @Override
    public Long lpush(String key,String... string){
        return pool.execute(key,(k,jedis)->{
            return jedis.lpush(k,string);
        });
    }

    @Override
    public Long llen(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.llen(k);
        });
    }

    @Override
    public List<String> lrange(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.lrange(k,start,end);
        });
    }

    @Override
    public String ltrim(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.ltrim(k,start,end);
        });
    }

    @Override
    public String lindex(String key,long index){
        return pool.execute(key,(k,jedis)->{
            return jedis.lindex(k,index);
        });
    }

    @Override
    public String lset(String key,long index,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.lset(k,index,value);
        });
    }

    @Override
    public Long lrem(String key,long count,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.lrem(k,count,value);
        });
    }

    @Override
    public String lpop(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.lpop(k);
        });
    }

    @Override
    public String rpop(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.rpop(k);
        });
    }

    @Override
    public Long sadd(String key,String... member){
        return pool.execute(key,(k,jedis)->{
            return jedis.sadd(k,member);
        });
    }

    @Override
    public Set<String> smembers(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.smembers(k);
        });
    }

    @Override
    public Long srem(String key,String... member){
        return pool.execute(key,(k,jedis)->{
            return jedis.srem(k,member);
        });
    }

    @Override
    public String spop(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.spop(k);
        });
    }

    @Override
    public Set<String> spop(String key,long count){
        return pool.execute(key,(k,jedis)->{
            return jedis.spop(k,count);
        });
    }

    @Override
    public Long scard(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.scard(k);
        });
    }

    @Override
    public Boolean sismember(String key,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.sismember(k,member);
        });
    }

    @Override
    public String srandmember(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.srandmember(k);
        });
    }

    @Override
    public List<String> srandmember(String key,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.srandmember(k,count);
        });
    }

    @Override
    public Long strlen(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.strlen(k);
        });
    }

    @Override
    public Long zadd(String key,double score,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zadd(k,score,member);
        });
    }

    @Override
    public Long zadd(String key,double score,String member,ZAddParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.zadd(k,score,member,params);
        });
    }

    @Override
    public Long zadd(String key,Map<String,Double> scoreMembers){
        return pool.execute(key,(k,jedis)->{
            return jedis.zadd(k,scoreMembers);
        });
    }

    @Override
    public Long zadd(String key,Map<String,Double> scoreMembers,ZAddParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.zadd(k,scoreMembers,params);
        });
    }

    @Override
    public Set<String> zrange(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrange(k,start,end);
        });
    }

    @Override
    public Long zrem(String key,String... member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrem(k,member);
        });
    }

    @Override
    public Double zincrby(String key,double score,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zincrby(k,score,member);
        });
    }

    @Override
    public Double zincrby(String key,double score,String member,ZIncrByParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.zincrby(k,score,member,params);
        });
    }

    @Override
    public Long zrank(String key,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrank(k,member);
        });
    }

    @Override
    public Long zrevrank(String key,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrank(k,member);
        });
    }

    @Override
    public Set<String> zrevrange(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrange(k,start,end);
        });
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeWithScores(k,start,end);
        });
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeWithScores(k,start,end);
        });
    }

    @Override
    public Long zcard(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.zcard(k);
        });
    }

    @Override
    public Double zscore(String key,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.zscore(k,member);
        });
    }

    @Override
    public List<String> sort(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.sort(k);
        });
    }

    @Override
    public List<String> sort(String key,SortingParams sortingParameters){
        return pool.execute(key,(k,jedis)->{
            return jedis.sort(k,sortingParameters);
        });
    }

    @Override
    public Long zcount(String key,double min,double max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zcount(k,min,max);
        });
    }

    @Override
    public Long zcount(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zcount(k,min,max);
        });
    }

    @Override
    public Set<String> zrangeByScore(String key,double min,double max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScore(k,min,max);
        });
    }

    @Override
    public Set<String> zrangeByScore(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScore(k,min,max);
        });
    }

    @Override
    public Set<String> zrevrangeByScore(String key,double max,double min){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScore(k,max,min);
        });
    }

    @Override
    public Set<String> zrangeByScore(String key,double min,double max,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScore(k,min,max,offset,count);
        });
    }

    @Override
    public Set<String> zrevrangeByScore(String key,String max,String min){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScore(k,max,min);
        });
    }

    @Override
    public Set<String> zrangeByScore(String key,String min,String max,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScore(k,min,max,offset,count);
        });
    }

    @Override
    public Set<String> zrevrangeByScore(String key,double max,double min,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScore(k,max,min,offset,count);
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,double min,double max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScoreWithScores(k,min,max);
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,double max,double min){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScoreWithScores(k,max,min);
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,double min,double max,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScoreWithScores(k,min,max,offset,count);
        });
    }

    @Override
    public Set<String> zrevrangeByScore(String key,String max,String min,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScore(k,max,min,offset,count);
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScoreWithScores(k,min,max);
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,String max,String min){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScoreWithScores(k,max,min);
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,String min,String max,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByScoreWithScores(k,min,max,offset,count);
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,double max,double min,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScoreWithScores(k,max,min,offset,count);
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,String max,String min,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByScoreWithScores(k,max,min,offset,count);
        });
    }

    @Override
    public Long zremrangeByRank(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zremrangeByRank(k,start,end);
        });
    }

    @Override
    public Long zremrangeByScore(String key,double start,double end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zremrangeByScore(k,start,end);
        });
    }

    @Override
    public Long zremrangeByScore(String key,String start,String end){
        return pool.execute(key,(k,jedis)->{
            return jedis.zremrangeByScore(k,start,end);
        });
    }

    @Override
    public Long zlexcount(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zlexcount(k,min,max);
        });
    }

    @Override
    public Set<String> zrangeByLex(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByLex(k,min,max);
        });
    }

    @Override
    public Set<String> zrangeByLex(String key,String min,String max,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrangeByLex(k,min,max,offset,count);
        });
    }

    @Override
    public Set<String> zrevrangeByLex(String key,String max,String min){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByLex(k,max,min);
        });
    }

    @Override
    public Set<String> zrevrangeByLex(String key,String max,String min,int offset,int count){
        return pool.execute(key,(k,jedis)->{
            return jedis.zrevrangeByLex(k,max,min,offset,count);
        });
    }

    @Override
    public Long zremrangeByLex(String key,String min,String max){
        return pool.execute(key,(k,jedis)->{
            return jedis.zremrangeByLex(k,min,max);
        });
    }

    @Override
    public Long linsert(String key,LIST_POSITION where,String pivot,String value){
        return pool.execute(key,(k,jedis)->{
            return jedis.linsert(k,where,pivot,value);
        });
    }

    @Override
    public Long lpushx(String key,String... string){
        return pool.execute(key,(k,jedis)->{
            return jedis.lpushx(k,string);
        });
    }

    @Override
    public Long rpushx(String key,String... string){
        return pool.execute(key,(k,jedis)->{
            return jedis.rpushx(k,string);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public List<String> blpop(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.blpop(k);
        });
    }

    @Override
    public List<String> blpop(int timeout,String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.blpop(timeout,k);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public List<String> brpop(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.brpop(k);
        });
    }

    @Override
    public List<String> brpop(int timeout,String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.brpop(timeout,k);
        });
    }

    @Override
    public String echo(String string){
        return pool.execute(string,(k,jedis)->{
            return jedis.echo(k);
        });
    }

    @Override
    public Long move(String key,int dbIndex){
        return pool.execute(key,(k,jedis)->{
            return jedis.move(k,dbIndex);
        });
    }

    @Override
    public Long bitcount(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.bitcount(k);
        });
    }

    @Override
    public Long bitcount(String key,long start,long end){
        return pool.execute(key,(k,jedis)->{
            return jedis.bitcount(k,start,end);
        });
    }

    @Override
    public Long bitpos(String key,boolean value){
        return pool.execute(key,(k,jedis)->{
            return jedis.bitpos(k,value);
        });
    }

    @Override
    public Long bitpos(String key,boolean value,BitPosParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.bitpos(k,value,params);
        });
    }
    @Override
    public List<Long> bitfield(String key,String... arguments){
        return pool.execute(key,(k,jedis)->{
            return jedis.bitfield(k,arguments);
        });
    }
    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public ScanResult<Entry<String,String>> hscan(String key,int cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.hscan(k,cursor);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public ScanResult<String> sscan(String key,int cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.sscan(k,cursor);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public ScanResult<Tuple> zscan(String key,int cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.zscan(k,cursor);
        });
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String key,String cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.hscan(k,cursor);
        });
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String key,String cursor,ScanParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.hscan(k,cursor,params);
        });
    }

    @Override
    public ScanResult<String> sscan(String key,String cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.sscan(k,cursor);
        });
    }

    @Override
    public ScanResult<String> sscan(String key,String cursor,ScanParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.sscan(k,cursor,params);
        });
    }

    @Override
    public ScanResult<Tuple> zscan(String key,String cursor){
        return pool.execute(key,(k,jedis)->{
            return jedis.zscan(k,cursor);
        });
    }

    @Override
    public ScanResult<Tuple> zscan(String key,String cursor,ScanParams params){
        return pool.execute(key,(k,jedis)->{
            return jedis.zscan(k,cursor,params);
        });
    }

    @Override
    public Long pfadd(String key,String... elements){
        return pool.execute(key,(k,jedis)->{
            return jedis.pfadd(k,elements);
        });
    }

    @Override
    public long pfcount(String key){
        return pool.execute(key,(k,jedis)->{
            return jedis.pfcount(k);
        });
    }

    @Override
    public Long geoadd(String key,double longitude,double latitude,String member){
        return pool.execute(key,(k,jedis)->{
            return jedis.geoadd(k,longitude,latitude,member);
        });
    }

    @Override
    public Long geoadd(String key,Map<String,GeoCoordinate> memberCoordinateMap){
        return pool.execute(key,(k,jedis)->{
            return jedis.geoadd(k,memberCoordinateMap);
        });
    }

    @Override
    public Double geodist(String key,String member1,String member2){
        return pool.execute(key,(k,jedis)->{
            return jedis.geodist(k,member1,member2);
        });
    }

    @Override
    public Double geodist(String key,String member1,String member2,GeoUnit unit){
        return pool.execute(key,(k,jedis)->{
            return jedis.geodist(k,member1,member2,unit);
        });
    }

    @Override
    public List<String> geohash(String key,String... members){
        return pool.execute(key,(k,jedis)->{
            return jedis.geohash(k,members);
        });
    }

    @Override
    public List<GeoCoordinate> geopos(String key,String... members){
        return pool.execute(key,(k,jedis)->{
            return jedis.geopos(k,members);
        });
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key,double longitude,double latitude,double radius,GeoUnit unit){
        return pool.execute(key,(k,jedis)->{
            return jedis.georadius(k,longitude,latitude,radius,unit);
        });
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key,double longitude,double latitude,double radius,GeoUnit unit,GeoRadiusParam param){
        return pool.execute(key,(k,jedis)->{
            return jedis.georadius(k,longitude,latitude,radius,unit,param);
        });
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key,String member,double radius,GeoUnit unit){
        return pool.execute(key,(k,jedis)->{
            return jedis.georadiusByMember(k,member,radius,unit);
        });
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key,String member,double radius,GeoUnit unit,GeoRadiusParam param){
        return pool.execute(key,(k,jedis)->{
            return jedis.georadiusByMember(k,member,radius,unit,param);
        });
    }

    @Override
    public ShardedJedis getResource(){
        throw new UnsupportedOperationException("you can invoke every redis command directly");
    }

    @Override
    public void returnResource(ShardedJedis jedis){
        throw new UnsupportedOperationException("getResource() is useless, and so do this one");
    }

    @Override
    public void returnBrokenResource(ShardedJedis jedis){
        throw new UnsupportedOperationException("getResource() is useless, and so do this one");
    }
    @Deprecated
    @Override
    public <E> E execute(MethodHandle cmd,String key,Object... args){
        throw new UnsupportedOperationException("this method is deprecated");
    }

    @Override
    public long inc(String key){
        return incr(key);
    }

    @Override
    public long dec(String key){
        return decr(key);
    }

    @Override
    public Long del(String key){
        return pool.execute(key,(k,redis)->{
            return redis.del(k);
        });
    }

    @Override
    public Long del(String... key){
        long r=0;
        for(int i=0,l=key.length;i<l;i++){
            r+=del(key[i]);
        }
        return r;
    }

    @Override
    public long incBy(String key,long value){
        return incrBy(key,value);
    }

    @Override
    public long decBy(String key,long value){
        return decrBy(key,value);
    }

    @Override
    public String generateKey(Object... parts){
        return Help.concat(parts, ":");
    }
    
    @Deprecated
    @Override
    public String getSet(String key){
        throw new UnsupportedOperationException("this method is not implemented");
    }

    @Override
    public String getString(String key){
        return get(key);
    }

    @Override
    public Boolean getBoolean(String key){
        return Boolean.valueOf(getString(key));
    }

    @Override
    public Long getLong(String key){
        return Long.parseLong(getString(key));
    }

    @Override
    public Integer getInteger(String key){
        return Integer.parseInt(getString(key));
    }

    @Override
    public BigDecimal getBigDecimal(String key){
        return new BigDecimal(getString(key));
    }

    @Override
    public Long hgetLong(String key,String field){
        return Long.parseLong(hget(key,field));
    }

    @Override
    public Integer hgetInteger(String key,String field){
        return Integer.parseInt(hget(key,field));
    }

    @Override
    public BigDecimal hgetBigDecimal(String key,String field){
        return new BigDecimal(hget(key,field));
    }

    @Override
    public Long lindexLong(String key,long index){
        return Long.parseLong(lindex(key,index));
    }

    @Override
    public Integer lindexInteger(String key,long index){
        return Integer.parseInt(lindex(key,index));
    }

    @Override
    public BigDecimal lindexBigDecimal(String key,long index){
        return new BigDecimal(lindex(key,index));
    }

    @Override
    public String get(String key){
        return pool.execute(key,(k,redis)->{
            return redis.get(k);
        });
    }

    @Override
    public <E> E get(String key,Class<E> cls){
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
    
    @Deprecated
    @Override
    public Long set(String key,Map<String,String> value){
        throw new UnsupportedOperationException("hmset(String,Map) instead");
    }

    @Override
    public String set(String key,Object value){
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
    private String value2String(Object v){
        if(v==null){
            return null;
        }
        return v instanceof Date?Long.toString(((Date)v).getTime()):v.toString();
    }
    @Override
    public String set(String key,Object value,String... fields){
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
    public String set(String key,String value){
        return pool.execute(key,(k,redis)->{
            return redis.set(k,value);
        });
    }

    @Override
    public Long set(String key,String field,Object value){
        return hset(key,field,value2String(value));
    }

    @Override
    public String setex(String key,int seconds,Object value){
        return setex(key,seconds,value2String(value));
    }

    @Override
    public Long setnx(String key,Object value){
        return setnx(key,value2String(value));
    }

    @Override
    public void hsetnx(String key,Object value){
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
    public Long hsetnx(String key,String field,Object value){
        return this.hsetnx(key, field, value2String(value));
    }

    @Override
    public Long hset(String key,String field,Object value){
        return this.hset(key, field, value2String(value));
    }

    @Override
    public Long sadd(String key,Object... values){
        if(values==null){
            sadd(key,null);
        }
        String[] ms=new String[values.length];
        for(int i=0,l=values.length;i<l;i++){
            ms[i]=value2String(values[i]);
        }
        return sadd(key,ms);
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
    public int compare(String key,Comparable toCompare){
        return compareTo(get(key),toCompare);
    }

    @Override
    public int compare(String key,String field,Comparable toCompare){
        return compareTo(hget(key,field),toCompare);
    }

    @Override
    public int compare(String key,long index,Comparable toCompare){
        return compareTo(lindex(key,index),toCompare);
    }

    @Override
    public int hcompare(String key,String field,Comparable toCompare){
        return compareTo(hget(key,field),toCompare);
    }

    @Override
    public int compare(String key,Class<Comparable> cls,Comparable toCompare){
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
    public int del(List keyParts,String... prefix){
        String pre=this.generateKey(prefix);
        int c=0;
        for(int i=0,l=keyParts.size();i<l;i++){
            c+=del(this.generateKey(pre,keyParts.get(i))).intValue();
        }
        return c;
    }

    @Override
    public int del(Object[] keyParts,String... prefix){
        String pre=this.generateKey(prefix);
        int c=0;
        for(int i=0,l=keyParts.length;i<l;i++){
            c+=del(this.generateKey(pre,keyParts[i])).intValue();
        }
        return c;
    }

    @Override
    public Long srem(String key,Object... members){
        String[] ms=new String[members.length];
        for(int i=0,l=members.length;i<l;i++){
            ms[i]=value2String(members[i]);
        }
        return srem(key,ms);
    }

    @Override
    public String hmset(String key,Object value){
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
            return hmset(key,m);
        }
    }

    @Override
    public Map<String,Object> getMap(String key){
        return new HashMap<String,Object>(hgetAll(key));
    }

    @Override
    public Map<String,String> getMap(String key,String... fields){
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
    public Map<String,Object> getJsonMap(String key){
        return getObjectFromJson(key,Map.class);
    }

    @Override
    public Map<String,Object> getJsonMap(String key,String... fields){
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
    public <E> E getObjectFromJson(String key,Class<E> cls){
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
    public String setJsonFromObject(String key,Object value){
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
    public <E> E hmget(String key,Class<E> cls,String... fields) throws Exception{
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

    @Override
    public String hmset(String key,Map<String,Object> hash,String... fields){
        Map m=new HashMap();
        if(Help.isNotEmpty(fields)){
            for(int i=0,l=fields.length;i<l;i++){
                Object v=hash.get(fields[i]);
                if(v!=null){
                    m.put(fields[i],value2String(v));
                }
            }
        }
        return hmset(key,m);
    }

    @Override
    public String hmset(String key,Object hash,String... fields){
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
        return hmset(key,m);
    }

    @Override
    public void hsetnx(String key,Object value,String... fields){
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
    public void hsetnx(String key,Map<String,Object> value){
        for(Map.Entry<String,Object> e:value.entrySet()){
            hsetnx(e.getKey(),value2String(e.getValue()));
        }
    }

    @Override
    public void hsetnx(String key,Map<String,Object> value,String... fields){
        for(int i=0,l=fields.length;i<l;i++){
            hsetnx(key,fields[i],value2String(value.get(fields[i])));
        }
    }

    @Override
    public List<String> lgetAll(String key){
        return lrange(key,0,this.llen(key));
    }

    @Override
    public Long lpush(String key,Object... value){
        String[] vs=new String[value.length];
        for(int i=0,l=value.length;i<l;i++){
            vs[i]=value2String(value[i]);
        }
        return lpush(key,vs);
    }

    @Override
    public Long lpushx(String key,String value){
        return pool.execute(key,(k,redis)->{
            return redis.lpushx(k,value);
        });
    }

    @Override
    public Long rpushx(String key,String value){
        return pool.execute(key,(k,redis)->{
            return redis.rpushx(k,value);
        });
    }

    @Override
    public Long lpush(String key,Collection value){
        return lpush(key,value.toArray());
    }

    @Override
    public String zindex(String key,long index){
        Set<String> s=zrange(key,index,index);
        return s!=null?null:s.iterator().next();
    }

    @Override
    public int zindexInteger(String key,long index){
        return Integer.parseInt(zindex(key,index));
    }

    @Override
    public long zindexLong(String key,long index){
        return Long.parseLong(zindex(key,index));
    }

    @Override
    public String zfirst(String key){
        return zindex(key,0);
    }

    @Override
    public Long expire(String key,long expire){
        return expire(key,(int)expire);
    }

    @Override
    public void delKeys(String keyPattern){
        pool.iterator().forEachRemaining((p)->{
            p.execute((redis)->{
                redis.keys(keyPattern).forEach((k)->{
                    redis.del(k);
                });
                return "OK";
            });
        });
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
        return pool.execute(keys[0],(k,redis)->{
            int l=0;
            if(args!=null){
                l=keys.length+args.length;
            }else{
                l=keys.length;
            }
            String[] params=new String[l];
            System.arraycopy(keys, 0, params, 0, keys.length);
            System.arraycopy(args, 0, params, keys.length, args.length);
            return eval(redis,sha,script,keys.length,params);
        });
    }
    @Override
    public Object eval(String script,String[] keys,String... args){
        return eval(false,script,keys,args);
    }

    @Override
    public Object evalsha(String sha,String[] keys,String... args){
        return eval(true,sha,keys,args);
    }

    @Override
    public String scriptLoad(String key,String script){
        return pool.execute(key,(k,redis)->{
            return redis.scriptLoad(script);
        });
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
                                + "if ((ARGV[2]<0 and v-ARGV[2]>=ARGV[3]) or (ARGV[2]>0 and v+ARGV[2]<=ARGV[3])) then \r\n"
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
    
    private String zincrByCompare;
    private String loadZincrByCompare(String key){
        if(zincrByCompare==null){
            synchronized(this){
                if(zincrByCompare==null){
                    String script="local v=redis.call('zscore',KEYS[1],ARGV[1])\r\n"
                                + "if (~v) then \r\n"
                                + "    v=0 \r\n"
                                + "end"
                                + "if ((ARGV[2]<0 and v-ARGV[2]>=ARGV[3]) or (ARGV[2]>0 and v+ARGV[2]<=ARGV[3])) then \r\n"
                                + "    return redis.call('zincrby',KEYS[1],ARGV[2],ARGV[1]) \r\n"
                                + "end \r\n"
                                + "return v";
                    hincrByCompare=scriptLoad(key,script);
                }
            }
        }
        return hincrByCompare;
    }
    @Override
    public long zincrByCompare(String key,String name,long incr,long val){
        return (Long)evalsha(loadZincrByCompare(key),new String[]{key},name,Long.toString(incr),Long.toString(val));
    }
}
