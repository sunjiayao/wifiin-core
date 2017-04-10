package com.wifiin.nio.netty;

import java.util.Collections;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;

import io.netty.channel.ChannelOption;

public class NettyServerParams<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>> extends NettyGeneralParams<I,O,T>{
    /**
     * 
     */
    private static final long serialVersionUID=-3355915629046589296L;
    private Map<ChannelOption,Object> childChannelOptions=Maps.newHashMap();
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
}
