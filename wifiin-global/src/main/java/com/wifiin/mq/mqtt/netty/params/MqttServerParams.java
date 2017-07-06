package com.wifiin.mq.mqtt.netty.params;

import com.wifiin.mq.mqtt.netty.heartbeat.HeartbeatExecutor;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.NettyClientParams;
import com.wifiin.nio.netty.channel.ChannelConnectChecker;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;

public class MqttServerParams<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>> extends NettyClientParams<I,O,T>{

    /**
     * 
     */
    private static final long serialVersionUID=-8503766182179515775L;

    public ChannelConnectChecker channelConnectChecker(){
        return new HeartbeatExecutor();
    }
}
