package com.wifiin.nio.netty;

import java.util.Collections;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

public class NettyServerParams<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>> extends NettyGeneralParams<I,O,T>{
    /**
     * 
     */
    private static final long serialVersionUID=-3355915629046589296L;
    private Map<ChannelOption,Object> childChannelOptions=Maps.newHashMap();
    private int poolCount=Runtime.getRuntime().availableProcessors()*2;
    
    public void poolCount(int poolCount){
        this.poolCount=poolCount;
    }
    public int poolCount(){
        return poolCount;
    }
    public void backlog(int backlog){
        super.addChannelOption(ChannelOption.SO_BACKLOG,backlog);
    }
    public void reuseaddr(boolean reuseaddr){
        super.addChannelOption(ChannelOption.SO_REUSEADDR,reuseaddr);
    }
    public void addChildChannelOption(ChannelOption option,Object value){
        childChannelOptions.put(option,value);
    }
    public Map<ChannelOption,Object> childChannelOptions(){
        return Collections.unmodifiableMap(childChannelOptions);
    }
    public EventLoopGroup newAcceptorEventLoopGroup(String groupName){
        return super.newEventLoopGroup(groupName,1);
    }
}
