package com.wifiin.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.google.common.collect.Lists;
import com.wifiin.redis.JedisConnection;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

public class TestJedisConnection{
    public static void main(String[] args){
        ShardedJedisPool pool=null;
        try{
            JedisShardInfo shard=new JedisShardInfo("172.16.1.5",6379);
            shard.setPassword("_834smMim7");
            pool=new ShardedJedisPool(new GenericObjectPoolConfig(),Lists.newArrayList(shard));
            
            JedisConnection jedis=new JedisConnection(pool);
            
            jedis.set("testSET","1");
            System.out.println(jedis.get("testSET"));
            jedis.del("testSET");
            jedis.hmset("testHMSET",new Test());
            System.out.println(jedis.hgetAll("testHMSET"));
            jedis.del("testHMSET");
        }finally{
            pool.close();
        }
    }
    public static class  Test{
        private int test;
        public int getTest(){
            return test;
        }
        public void setTest(int test){
            this.test=test;
        }
    }
}
