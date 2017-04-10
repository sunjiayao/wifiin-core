package com.wifiin.nio.netty;

import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;

import io.netty.channel.ChannelOption;

public class NettyClientParams<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>> extends NettyGeneralParams<I,O,T>{
    /**
     * 
     */
    private static final long serialVersionUID=-2909842413752163131L;
    private String host;
    public void host(String host){
        this.host=host;
    }
    public String host(){
        return host;
    }
    public void connectionTimeoutMillis(int millis){
        super.addChannelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS,millis);
    }
    public long connectionTimeoutMillis(){
        Long millis=(Long)super.channelOptions().get(ChannelOption.CONNECT_TIMEOUT_MILLIS);
        return millis==null?3000:millis;
    }
}
