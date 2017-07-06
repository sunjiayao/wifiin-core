package com.wifiin.multilanguage;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.wifiin.redis.RedisConnection;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

@Component
public class MockRedis implements RedisConnection{

    @Override
    public String set(String key,String value,String nxxx,String expx,long time){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String set(String key,String value,String nxxx){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean exists(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long persist(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String type(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long expire(String key,int seconds){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long pexpire(String key,long milliseconds){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long expireAt(String key,long unixTime){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long pexpireAt(String key,long millisecondsTimestamp){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long ttl(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long pttl(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean setbit(String key,long offset,boolean value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean setbit(String key,long offset,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getbit(String key,long offset){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long setrange(String key,long offset,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getrange(String key,long startOffset,long endOffset){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSet(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long setnx(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setex(String key,int seconds,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String psetex(String key,long milliseconds,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long decrBy(String key,long integer){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long decr(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long incrBy(String key,long integer){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double incrByFloat(String key,double value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long incr(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long append(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String substr(String key,int start,int end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hset(String key,String field,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String hget(String key,String field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hsetnx(String key,String field,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String hmset(String key,Map<String,String> hash){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> hmget(String key,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hincrBy(String key,String field,long value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double hincrByFloat(String key,String field,double value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean hexists(String key,String field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hdel(String key,String... field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hlen(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> hkeys(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> hvals(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String,String> hgetAll(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long rpush(String key,String... string){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lpush(String key,String... string){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long llen(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> lrange(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String ltrim(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String lindex(String key,long index){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String lset(String key,long index,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lrem(String key,long count,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String lpop(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String rpop(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long sadd(String key,String... member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> smembers(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long srem(String key,String... member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String spop(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> spop(String key,long count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long scard(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean sismember(String key,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String srandmember(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> srandmember(String key,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long strlen(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String key,double score,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String key,double score,String member,ZAddParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String key,Map<String,Double> scoreMembers){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zadd(String key,Map<String,Double> scoreMembers,ZAddParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrange(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zrem(String key,String... member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double zincrby(String key,double score,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double zincrby(String key,double score,String member,ZIncrByParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zrank(String key,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zrevrank(String key,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrange(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zcard(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double zscore(String key,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> sort(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> sort(String key,SortingParams sortingParameters){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zcount(String key,double min,double max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zcount(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key,double min,double max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String key,double max,double min){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key,double min,double max,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String key,String max,String min){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key,String min,String max,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String key,double max,double min,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,double min,double max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,double max,double min){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,double min,double max,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByScore(String key,String max,String min,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,String max,String min){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key,String min,String max,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,double max,double min,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key,String max,String min,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zremrangeByRank(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zremrangeByScore(String key,double start,double end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zremrangeByScore(String key,String start,String end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zlexcount(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrangeByLex(String key,String min,String max,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByLex(String key,String max,String min){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> zrevrangeByLex(String key,String max,String min,int offset,int count){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long zremrangeByLex(String key,String min,String max){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long linsert(String key,LIST_POSITION where,String pivot,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lpushx(String key,String... string){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long rpushx(String key,String... string){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> blpop(String arg){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> blpop(int timeout,String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> brpop(String arg){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> brpop(int timeout,String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String echo(String string){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long move(String key,int dbIndex){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long bitcount(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long bitcount(String key,long start,long end){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long bitpos(String key,boolean value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long bitpos(String key,boolean value,BitPosParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String key,int cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<String> sscan(String key,int cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String key,int cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String key,String cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Entry<String,String>> hscan(String key,String cursor,ScanParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<String> sscan(String key,String cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<String> sscan(String key,String cursor,ScanParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String key,String cursor){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScanResult<Tuple> zscan(String key,String cursor,ScanParams params){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long pfadd(String key,String... elements){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long pfcount(String key){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Long geoadd(String key,double longitude,double latitude,String member){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long geoadd(String key,Map<String,GeoCoordinate> memberCoordinateMap){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double geodist(String key,String member1,String member2){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double geodist(String key,String member1,String member2,GeoUnit unit){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> geohash(String key,String... members){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoCoordinate> geopos(String key,String... members){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key,double longitude,double latitude,double radius,GeoUnit unit){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key,double longitude,double latitude,double radius,GeoUnit unit,
            GeoRadiusParam param){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key,String member,double radius,GeoUnit unit){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key,String member,double radius,GeoUnit unit,
            GeoRadiusParam param){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> bitfield(String key,String... arguments){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ShardedJedis getResource(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void returnResource(ShardedJedis jedis){
        // TODO Auto-generated method stub
        
    }

    @Override
    public void returnBrokenResource(ShardedJedis jedis){
        // TODO Auto-generated method stub
        
    }

    @Override
    public <E> E execute(MethodHandle cmd,String key,Object... args){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long inc(String key){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long dec(String key){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Long del(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long del(String... key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long incBy(String key,long value){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long decBy(String key,long value){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String generateKey(Object... parts){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSet(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getBoolean(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getLong(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getInteger(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hgetLong(String key,String field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer hgetInteger(String key,String field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal hgetBigDecimal(String key,String field){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lindexLong(String key,long index){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer lindexInteger(String key,long index){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal lindexBigDecimal(String key,long index){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String get(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E> E get(String key,Class<E> cls){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long set(String key,Map<String,String> value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String set(String key,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String set(String key,Object value,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String set(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long set(String key,String field,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setex(String key,int seconds,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long setnx(String key,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void hsetnx(String key,Object value){
        // TODO Auto-generated method stub
        
    }

    @Override
    public Long hsetnx(String key,String field,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hset(String key,String field,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long sadd(String key,Object... values){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compare(String key,Comparable toCompare){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int compare(String key,String field,Comparable toCompare){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int compare(String key,long index,Comparable toCompare){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int hcompare(String key,String field,Comparable toCompare){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int compare(String key,Class<Comparable> cls,Comparable toCompare){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int del(List keyParts,String... prefix){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int del(Object[] keyParts,String... prefix){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Long srem(String key,Object... members){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String hmset(String key,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String,Object> getMap(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String,String> getMap(String key,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String,Object> getJsonMap(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String,Object> getJsonMap(String key,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E> E getObjectFromJson(String key,Class<E> cls){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setJsonFromObject(String key,Object value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setJsonFromObjectExpire(String key,Object value,int expire){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String setJsonFromObjectExpireAt(String key,Object value,long expireAt){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E> E hmget(String key,Class<E> cls,String... fields) throws Exception{
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String hmset(String key,Map<String,Object> hash,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String hmset(String key,Object hash,String... fields){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void hsetnx(String key,Object value,String... fields){
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hsetnx(String key,Map<String,Object> value){
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hsetnx(String key,Map<String,Object> value,String... fields){
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> lgetAll(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lpush(String key,Object... value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lpushx(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long rpushx(String key,String value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lpush(String key,Collection value){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String zindex(String key,long index){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int zindexInteger(String key,long index){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long zindexLong(String key,long index){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String zfirst(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long expire(String key,long expire){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delKeys(String keyPattern){
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object eval(String script,String[] keys,String... args){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object evalsha(String sha,String[] keys,String... args){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scriptLoad(String key,String script){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String compareAndSet(String key,String oldVal,String newVal){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long incrIfLessThan(String key,long ceil){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long incrByIfLessThan(String key,long incr,long ceil){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long decrIfLargerThan(String key,long floor){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long decrByIfLargerThan(String key,long decr,long floor){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long hincrByCompare(String key,String name,long incr,long val){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long zincrByCompare(String key,String name,long incr,long val){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <E> E getObjectFromJsonOrSupplier(String key,Class<E> cls,Supplier<E> supplier){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E> E getObjectFromJsonOrSupplier(String key,Class<E> cls,Supplier<E> supplier,int expire){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E> E getObjectFromJsonOrSupplier(String key,Class<E> cls,Supplier<E> supplier,long expireAt){
        // TODO Auto-generated method stub
        return null;
    }
    
}
