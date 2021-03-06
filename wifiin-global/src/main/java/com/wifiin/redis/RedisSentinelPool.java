package com.wifiin.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wifiin.common.GlobalObject;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

public class RedisSentinelPool extends Pool<ShardedJedis> {
    
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private Logger log = LoggerFactory.getLogger(RedisSentinelPool.class);
    private ThreadLocal<ShardedJedisPool> pool=new ThreadLocal<>();
    private volatile ShardedJedisPool shardedJedisPool;
    private Set<String> sentinel;
    private String password;
    private int timeout;
    private GenericObjectPoolConfig poolConfig;
    private volatile List<JedisShardInfo> list;
    private Set<JedisShardInfo> shards;
    
    public RedisSentinelPool(Set<String> sentinel, String password, int timeout, GenericObjectPoolConfig poolConfig) {
        list = initSentinel(sentinel, password);
        populateShards();
        this.sentinel = sentinel;
        this.password = password;
        this.timeout = timeout;
        this.poolConfig = poolConfig;
        shardedJedisPool = getShardedJedisPool(list);
        initMonitor();
    };
    
    public ShardedJedisPool getShardedJedisPool(List<JedisShardInfo> list){
        return new ShardedJedisPool(poolConfig, list);
    }
    
    private ShardedJedisPool pool(){
        ShardedJedisPool pool=this.pool.get();
        if(pool==null || pool.isClosed()){
            pool=this.shardedJedisPool;
            this.pool.set(pool);
        }
        return pool;
    }
    
    public ShardedJedis getResource(){
        return this.pool().getResource();
    }
    
    public void returnResourceObject(final ShardedJedis resource){
        this.pool().returnResourceObject(resource);
    }
    
    public void returnBrokenResource(final ShardedJedis resource){
        this.pool().returnBrokenResource(resource);
    }
    
    public void returnResource(final ShardedJedis resource){
        this.pool().returnResource(resource);
    }
    
    public void destroy(){
        this.shardedJedisPool.destroy();
    }
    
    private Jedis createJedis(String host, int port){
        return new Jedis(host, port);
    }
    
    private Jedis createJedis(String host, String port, String password){
        Jedis j = createJedis(host, Integer.parseInt(port));
        j.auth(password);
        return j;
    }
    
    private Jedis createJedis(HostAndPort hap){
        return createJedis(hap.getHost(), hap.getPort());
    }
    
    private List<Map<String, String>> sentinelMasters(String sentinelHost){
        HostAndPort hap = toHostAndPort(Arrays.asList(sentinelHost.split(":")));
        return sentinelMasters(hap);
    }
    
    private List<Map<String, String>> sentinelMasters(HostAndPort hap){
        Jedis jedis = null;
        try {
            jedis = createJedis(hap);
            return jedis.sentinelMasters();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    private List<JedisShardInfo> initSentinel(Set<String> sentinels, String password){
        List<JedisShardInfo> list = new ArrayList<JedisShardInfo>();
        HostAndPort master = null;
        boolean fetched = false;
        while (!fetched) {
            for (String sentinel : sentinels) {
                final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));
                List<Map<String, String>> mlist = sentinelMasters(hap);
                try {
                    if (null == master) {
                        for (int i = 0; i < mlist.size(); i++) {
                            Map<String, String> m = mlist.get(i);
                            JedisShardInfo info = new JedisShardInfo(m.get("ip"), Integer.parseInt(m.get("port")),
                                    m.get("name"));
                            info.setSoTimeout(this.timeout);
                            if (!m.get("flags").contains("s_down") && !m.get("flags").contains("disconnected")) {
                                info.setPassword(password);
                                list.add(info);
                                fetched = true;
                            }
                        }
                    }
                } catch (JedisConnectionException e) {
                    log.error("cannot connetion to sentinel ,trying next one", e);
                }
            }
            if (null == master) {
                await(1000);
            }
        }
        return list;
    }
    
    private static HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult){
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
        return new HostAndPort(host, port);
    }
    private void populateShards(){
        Comparator<JedisShardInfo> comparator=(Comparator<JedisShardInfo>)(JedisShardInfo i1, JedisShardInfo i2)->{
            int r = i1.getHost().compareTo(i2.getHost());
            if(r==0){
                r=i1.getPort()-i2.getPort();
            }
            return r;
        };
        shards=Sets.newTreeSet(comparator);
        shards.addAll(list);
    }
    private void switchShardedPool(){
        log.info("RedisSentinelPool.switchShardedPool:start");
        if(list.size()==shards.size()){
            int i=0,l=list.size();
            for(;i<l && shards.contains(list.get(i));i++);
            if(i>=l){
                return;
            }
        }
        populateShards();
        List<JedisShardInfo> list=this.list=Lists.newArrayList(shards);
        ShardedJedisPool old = this.shardedJedisPool;
        ShardedJedisPool newPool=null;
        try{
            newPool=getShardedJedisPool(list);
            this.shardedJedisPool = newPool;
            log.info("RedisSentinelPool.switchShardedPool:ShardedInfo:"+GlobalObject.getJsonMapper().writeValueAsString(shards));
        }catch(Exception e){
            this.shardedJedisPool=old;
            if(newPool!=null){
                newPool.close();
            }
        }
        if(this.shardedJedisPool!=old && old!=null){
            old.close();
        }
        log.info("RedisSentinelPool.switchShardedPool:end");
    }
    
    private void psubscribe(Map<String, String> hostAndPort){
        while (true) {
            try {
                await(30000);
                psubscribe(hostAndPort.get("ip"), hostAndPort.get("port"));
            } catch (Exception e) {
            }
        }
    }
    
    private void psubscribe(String ip, String port){
        Jedis j = null;
        try {
            j = createJedis(ip, port, password);
            psubscribe(j);
        } finally {
            if (j != null) {
                try {
                    j.close();
                } catch (Exception e) {
                }
            }
        }
    }
    
    private void await(long wait){
        synchronized (this) {
            while (true) {
                try {
                    this.wait(wait);
                    return;
                } catch (Exception e) {
                }
            }
        }
    }
    
    private void psubscribe(final Jedis j){
        j.psubscribe(new JedisPubSub() {
            public void onMessage(String channel, String message){
            }
            
            public void onPMessage(String mes1, String mes2, String mes3){
                synchronized(list){
                    Set<String> sentinels = RedisSentinelPool.this.sentinel;
                    for (String sentinel : sentinels) {
                        List<Map<String, String>> lists = sentinelMasters(sentinel);
                        for (int i = 0; i < lists.size(); i++) {
                            Map<String, String> m = lists.get(i);
                            if (m.get("flags").contains("s_down") || m.get("flags").contains("disconnected")) {
                                for (int j = 0; j < list.size(); j++) {
                                    if (list.get(j).getHost().equals(m.get("ip"))
                                            && list.get(j).getPort() == Integer.parseInt(m.get("port"))) {
                                        list.remove(j);
                                    }
                                }
                            } else {
                                boolean b = isExistJedisShardInfo(m.get("ip"), Integer.parseInt(m.get("port")), list);
                                if (!b) {
                                    JedisShardInfo js = new JedisShardInfo(m.get("ip"), Integer.parseInt(m.get("port")));
                                    js.setPassword(password);
                                    js.setSoTimeout(timeout);
                                    js.createResource().flushAll();
                                    list.add(js);
                                }
                            }
                        }
                    }
                    switchShardedPool();
                    await(30000);
                }
            }
            
            public void onPSubscribe(String arg0, int arg1){
            }
            
            public void onPUnsubscribe(String arg0, int arg1){
            }
            
            public void onSubscribe(String arg0, int arg1){
            }
            
            public void onUnsubscribe(String arg0, int arg1){
            }
        }, "__sentinel__:hello");
    }
    
    public void initMonitor(){
        executor.submit(new Runnable() {
            public void run(){
                Set<String> sentinels = RedisSentinelPool.this.sentinel;
                for (String sentinel : sentinels) {
                    List<Map<String, String>> lists = sentinelMasters(sentinel);
                    for (int i = 0; i < lists.size(); i++) {
                        Map<String, String> m = lists.get(i);
                        try {
                            if (m.get("flags").contains("s_down") || m.get("flags").contains("disconnected")) {
                                for (int j = 0; j < list.size(); j++) {
                                    JedisShardInfo jif = list.get(i);
                                    if (jif.getPort() == Integer.parseInt(m.get("port"))
                                            && jif.getHost().equals(m.get("ip"))) {
                                        list.remove(j);
                                    }
                                }
                            } else {
                                psubscribe(m);
                            }
                        } catch (JedisConnectionException e) {
                            if (m.get("flags").contains("s_down") || m.get("flags").contains("disconnected")) {
                                for (int j = 0; j < list.size(); j++) {
                                    JedisShardInfo jif = list.get(i);
                                    if (jif.getPort() == Integer.parseInt(m.get("port"))
                                            && jif.getHost().equals(m.get("ip"))) {
                                        list.remove(j);
                                    }
                                }
                            }
                        }
                    }
                }
                switchShardedPool();
            }
        });
    }
    
    public boolean isExistJedisShardInfo(String ip, int port, List<JedisShardInfo> list){
        boolean b = false;
        for (int i = 0; i < list.size(); i++) {
            JedisShardInfo js = list.get(i);
            if (ip.equals(js.getHost()) && port == js.getPort()) {
                b = true;
            } else {
                continue;
            }
        }
        return b;
    }
    
}
