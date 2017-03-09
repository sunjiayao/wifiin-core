package com.wifiin.pool.redis;

import com.wifiin.pool.KeyPooledObjectFactory;
import com.wifiin.pool.PoolConfig;
import com.wifiin.pool.PoolKey;

import redis.clients.jedis.Jedis;

public class PooledJedisFactory<K extends PoolKey> implements KeyPooledObjectFactory<RedisPoolKey,Jedis>{
    private ShardedRedisPoolConfig<Jedis> config;
    private RedisPoolKey key;
    public PooledJedisFactory(){}
    public PooledJedisFactory(RedisPoolKey key,PoolConfig<Jedis> config){
        setPoolKey(key);
        setPoolConfig(config);
    }
    public PooledJedisFactory(RedisPoolKey key,ShardedRedisPoolConfig<Jedis> config){
        setPoolKey(key);
        setPoolConfig(config);
    }
    public Jedis create(){
        Jedis jedis=new Jedis(key.getHost(),key.getPort(),config.getConnectionTimeout(),config.getSoTimeout());
        jedis.auth(config.getPassword());
        return jedis;
    }

    @Override
    public boolean validate(Jedis o){
        return "PONG".equals(o.ping());
    }

    @Override
    public void destroy(Jedis o){
        o.close();
    }

    @Override
    public Jedis activate(Jedis o){
        return o;
    }

    @Override
    public Jedis deactivate(Jedis o){
        return o;
    }

    @Override
    public void setPoolConfig(PoolConfig<Jedis> config){
        this.config=(ShardedRedisPoolConfig<Jedis>)config;
    }

    @Override
    public void setPoolKey(RedisPoolKey key){
        this.key=key;
    }
    
}
