package com.wifiin.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Hashing;

public class ShardedJedisSentinelPool extends ShardedJedisPool {
	private static final ExecutorService monitorExecutor=Executors.newFixedThreadPool(1);
	private static LinkedBlockingQueue<ShardedJedisSentinelPool> pools=new LinkedBlockingQueue<>();
	private List<JedisShardInfo> sentinels;
	private JedisShardInfo shardInfoTemplate;
	private Map<String,JedisShardInfo> shardInfoMap;
	public ShardedJedisSentinelPool(List<JedisShardInfo> sentinels,List<String> masterName, JedisShardInfo shardInfoTemplate, GenericKeyedObjectPoolConfig poolConfig) {
		this(sentinels,masterName,shardInfoTemplate,Hashing.MURMUR_HASH, poolConfig);
	}
	public ShardedJedisSentinelPool(List<JedisShardInfo> sentinels,List<String> masterName, JedisShardInfo shardInfoTemplate, Hashing algo, GenericKeyedObjectPoolConfig poolConfig) {
		this(sentinels,masterName,shardInfoTemplate, algo, null, poolConfig);
	}
	public ShardedJedisSentinelPool(List<JedisShardInfo> sentinels,List<String> masterName, JedisShardInfo shardInfoTemplate,Pattern keyTagPattern, GenericKeyedObjectPoolConfig poolConfig) {
		this(sentinels,masterName,shardInfoTemplate, Hashing.MURMUR_HASH, keyTagPattern, poolConfig);
	}
	public ShardedJedisSentinelPool(List<JedisShardInfo> sentinels,List<String> masterName, JedisShardInfo shardInfoTemplate, Hashing algo,Pattern keyTagPattern, GenericKeyedObjectPoolConfig poolConfig) {
		this(initSentinel(sentinels,masterName,shardInfoTemplate), algo, keyTagPattern, poolConfig);
		this.sentinels=sentinels;
		this.shardInfoTemplate=shardInfoTemplate;
		pools.offer(this);
	}
	private ShardedJedisSentinelPool(Map<String,JedisShardInfo> shardInfoMap,Hashing algo,Pattern keyTagPattern,GenericKeyedObjectPoolConfig poolConfig){
		super(new ArrayList(shardInfoMap.values()),algo,keyTagPattern,poolConfig);
		this.shardInfoMap=shardInfoMap;
	}
	@Override
	public void destroy(){
		try{
			for(ShardedJedisSentinelPool pool:pools){
				if(pool==this){
					pools.remove(this);
				}
			}
			if(pools.size()==0){
				monitorExecutor.shutdownNow();
			}
		}finally{
			super.destroy();
		}
	}
	private static Map<String,JedisShardInfo> initSentinel(final List<JedisShardInfo> sentinels,List<String> masterName,JedisShardInfo shardInfoTemplate){
		boolean running = true;
		Map<String,JedisShardInfo> map=new HashMap<>();
  		Set<JedisShardInfo> set=new HashSet<>();
		for(int j=0,jl=masterName.size();j<jl;j++){
			JedisShardInfo master=null;
			outer: while (running) {
	 			for (int i=0,il=sentinels.size();i<il;i++) {
	  				if(master==null){
		  				try {
		  					Jedis jedis = null;
		  					try{
			  					jedis=new Jedis(sentinels.get(i));
			  					String mn=masterName.get(j);
			  					map.put(mn, createJedisShardInfo(jedis.sentinelGetMasterAddrByName(mn),shardInfoTemplate));
			  					break outer;
		  					}finally{
		  						if(jedis!=null){
		  							jedis.shutdown();//.close();
		  						}
		  					}
		  				} catch (JedisConnectionException e) {}
	  				}
	  			}
		  		try {
		  			System.out.println(
		  				"All sentinels down, cannot determine where is "+ masterName + " master is running... sleeping 1000ms.");
		  			Thread.sleep(1000);
		  		} catch (InterruptedException e) {}
			}
		}
		initMonitor();
  		return map;
	}
	private static JedisShardInfo createJedisShardInfo(List<String> master, JedisShardInfo shardInfoTemplate) {
		JedisShardInfo info=new JedisShardInfo(master.get(0),Integer.parseInt(master.get(1)));
		info.setSoTimeout(shardInfoTemplate.getSoTimeout());
		return info;
	}
	private static void initMonitor(){
		monitorExecutor.submit(new Runnable(){
			@Override
			public void run() {
				while(true){
					LinkedBlockingQueue<ShardedJedisSentinelPool> queue=ShardedJedisSentinelPool.pools;
					try {
						final ShardedJedisSentinelPool pool = queue.take();
						queue.put(pool);
						for(final JedisShardInfo info:pool.sentinels){
							final Jedis j=new Jedis(info);
							try{
								final String channel="+switch-master";
								j.subscribe(new JedisPubSub() {
									@Override
									public void onSubscribe(String channel, int subscribedChannels) {
										super.unsubscribe();
									}
									@Override
									public void onPSubscribe(String pattern, int subscribedChannels) {}
									@Override
									public void onUnsubscribe(String channel, int subscribedChannels) {}
									@Override
									public void onPUnsubscribe(String pattern, int subscribedChannels) {}
									@Override
									public void onPMessage(String pattern, String channel, String message) {}
									@Override
									public void onMessage(String localChannel, String message) {
										if(channel.equals(localChannel)){
											String[] switchMasterMsg = message.split(" ");
											String masterName=switchMasterMsg[0];
										    if (switchMasterMsg.length > 3) {
												JedisShardInfo newShardInfo=createJedisShardInfo(Arrays.asList(
													    switchMasterMsg[3],
													    switchMasterMsg[4]), pool.shardInfoTemplate);
												JedisShardInfo info=pool.shardInfoMap.get(masterName);
												pool.shardInfoMap.put(masterName,newShardInfo);
												try {
													if(info!=null){
														pool.clear(info);
													}
													pool.addObject(newShardInfo);
												} catch (Exception e) {}
										    }else{
										    	JedisShardInfo info=pool.shardInfoMap.get(masterName);
										    	if(info!=null){
										    		pool.shardInfoMap.remove(masterName);
										    		pool.clear(info);
										    	}
										    }
										}
									}
								}, channel);
							}catch(Exception e){}
							Thread.sleep(1000);//just for waiting some time
						}
					} catch (InterruptedException e) {}
				}
			}});
	}
}
