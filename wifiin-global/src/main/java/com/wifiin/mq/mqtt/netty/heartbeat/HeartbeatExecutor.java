package com.wifiin.mq.mqtt.netty.heartbeat;

import java.util.Set;

import com.google.common.collect.Sets;
import com.wifiin.mq.mqtt.message.executors.MqttMessageExecutor;
import com.wifiin.nio.netty.channel.ChannelConnectChecker;

import io.netty.channel.Channel;

public class HeartbeatExecutor implements ChannelConnectChecker{
    private Set<Channel> idleChSet=Sets.newConcurrentHashSet();
    @Override
    public boolean start(Channel ch){
        if(idleChSet.add(ch)){
            writeAndFlush(ch,MqttMessageExecutor.PING_REQ_EXECUTOR);
            return true;
        }else{
            return false;
        }
    }
    @Override
    public void end(Channel ch){
        idleChSet.remove(ch);
    }
    @Override
    public void close0(Channel ch){
        writeAndFlush(ch,MqttMessageExecutor.DISCONNECT_EXECUTOR);
    }
    private void writeAndFlush(Channel ch,MqttMessageExecutor executor){
        ch.writeAndFlush(executor.execute(null));
    }
}
