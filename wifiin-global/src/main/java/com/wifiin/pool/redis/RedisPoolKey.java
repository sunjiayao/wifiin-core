package com.wifiin.pool.redis;

import com.wifiin.pool.PoolKey;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class RedisPoolKey implements PoolKey{
    private String host;
    private int port;
    private long weight=1;
    public RedisPoolKey(){}
    public RedisPoolKey(String host,int port){
        this.host=host;
        this.port=port;
    }
    public RedisPoolKey(String host,int port,long weight){
        this.host=host;
        this.port=port;
        this.weight=weight;
    }
    public String getHost(){
        return host;
    }
    public void setHost(String host){
        this.host=host;
    }
    public int getPort(){
        return port;
    }
    public void setPort(int port){
        this.port=port;
    }
    private String name;
    @Override
    public String name(){
        if(Help.isEmpty(name)){
            name=ThreadLocalStringBuilder.builder().append(host).append(';').append(port).toString();
        }
        return name;
    }

    @Override
    public long weight(){
        return weight;
    }
    
}
