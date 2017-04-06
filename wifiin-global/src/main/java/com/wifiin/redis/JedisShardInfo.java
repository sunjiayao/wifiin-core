package com.wifiin.redis;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JedisShardInfo extends redis.clients.jedis.JedisShardInfo{

	public JedisShardInfo(String host, int port, int timeout, String name) {
		super(host, port, timeout, name);
	}

	public JedisShardInfo(String host, int port, int timeout) {
		super(host, port, timeout);
	}

	public JedisShardInfo(String host, int port, String name) {
		super(host, port, name);
	}

	public JedisShardInfo(String host, int port) {
		super(host, port);
	}

	public JedisShardInfo(String host, String name) {
		super(host, name);
	}

	public JedisShardInfo(String host) {
		super(host);
	}
	private int hash;
	@Override
	public int hashCode(){
		if(hash==0){
			synchronized(this){
				if(hash==0){
					hash=new HashCodeBuilder().append(super.getHost()).append(super.getPort()).toHashCode();
				}
			}
		}
		return hash;
	}
	@Override
	public boolean equals(Object o){
		if(o!=null && o instanceof JedisShardInfo){
			JedisShardInfo jsi=(JedisShardInfo)o;
			return new EqualsBuilder().append(super.getHost(), jsi.getHost()).append(super.getPort(), jsi.getPort()).isEquals();
		}
		return false;
	}
}
