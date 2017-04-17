package com.wifiin.nio.netty;

import java.net.SocketAddress;

import org.slf4j.Logger;

import com.wifiin.log.LoggerFactory;
import com.wifiin.nio.netty.channel.ChannelConnectChecker;
import com.wifiin.nio.netty.util.NettyUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

class NettyConnectManageHandler extends ChannelDuplexHandler {
    private static final Logger log=LoggerFactory.getLogger(NettyUtil.NETTY_LOG_NAME);
    private String name;
    private ChannelConnectChecker channelChecker;
    NettyConnectManageHandler(String name,ChannelConnectChecker channelChecker){
        this.name=name.toUpperCase();
        this.channelChecker=channelChecker;
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("NETTY SERVER PIPELINE: channelRegistered {}", NettyUtil.parseChannelRemoteAddr(ctx));
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", NettyUtil.parseChannelRemoteAddr(ctx));
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", NettyUtil.parseChannelRemoteAddr(ctx));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", NettyUtil.parseChannelRemoteAddr(ctx));
        super.channelInactive(ctx);
    }
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) throws Exception {
        final String local = localAddress == null ? "UNKNOWN" : localAddress.toString();
        final String remote = remoteAddress == null ? "UNKNOWN" : remoteAddress.toString();
        log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
        super.connect(ctx, remoteAddress, localAddress, promise);
        NettyContext.params().afterConnectingChecker().start(ctx.channel());
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", NettyUtil.parseChannelRemoteAddr(ctx));
        super.disconnect(ctx, promise);
    }
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.info("NETTY {} PIPELINE: CLOSE {}",name, NettyUtil.parseChannelRemoteAddr(ctx));
        NettyContext.params().runnableBeforeClosing().run();
        super.close(ctx, promise);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            if (evnet.state().equals(IdleState.ALL_IDLE)) {
                log.warn("NETTY {} PIPELINE: IDLE exception [{}]",name, NettyUtil.parseChannelRemoteAddr(ctx));
                if(!channelChecker.start(ctx.channel())){
                    channelChecker.close(ctx);
                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("NETTY {} PIPELINE: exceptionCaught {}",name, NettyUtil.parseChannelRemoteAddr(ctx));
        log.warn("NETTY "+name+" PIPELINE: exceptionCaught exception.", cause);
        channelChecker.close(ctx);
    }
}