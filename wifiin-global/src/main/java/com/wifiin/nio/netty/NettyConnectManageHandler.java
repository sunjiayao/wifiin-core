package com.wifiin.nio.netty;

import java.util.function.Function;

import org.slf4j.Logger;

import com.wifiin.log.LoggerFactory;
import com.wifiin.nio.netty.util.NettyUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

class NettyConnectManageHandler extends ChannelDuplexHandler {
    private static final Logger log=LoggerFactory.getLogger(NettyUtil.NETTY_LOG_NAME);
    private String name;
    private Function<Channel,Boolean> idleChannelChecker;
    NettyConnectManageHandler(String name,Function<Channel,Boolean> idleChannelChecker){
        this.name=name.toUpperCase();
        this.idleChannelChecker=idleChannelChecker;
    }
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.info("NETTY {} PIPELINE: CLOSE {}",name, NettyUtil.parseChannelRemoteAddr(ctx));
        super.close(ctx, promise);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            if (evnet.state().equals(IdleState.ALL_IDLE)) {
                log.warn("NETTY {} PIPELINE: IDLE exception [{}]",name, NettyUtil.parseChannelRemoteAddr(ctx));
                if(!idleChannelChecker.apply(ctx.channel())){
                    ctx.close();
                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("NETTY {} PIPELINE: exceptionCaught {}",name, NettyUtil.parseChannelRemoteAddr(ctx));
        log.warn("NETTY "+name+" PIPELINE: exceptionCaught exception.", cause);
        ctx.close();
    }
}