package com.wifiin.nio.netty.util;

import java.net.SocketAddress;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.wifiin.log.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class NettyUtil{
    public static final String NETTY_LOG_NAME = "NettyLogName";
    private static final Logger log=LoggerFactory.getLogger(NETTY_LOG_NAME);
    public static void closeChannel(ChannelHandlerContext ctx){
        final String addrRemote = parseChannelRemoteAddr(ctx.channel());
        ctx.close().addListener((ChannelFuture future)->{
            log.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                future.isSuccess());
        });
    }
    public static ChannelFuture closeChannel(Channel channel) {
        final String addrRemote = parseChannelRemoteAddr(channel);
        return channel.close().addListener((ChannelFuture future)->{
            log.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                future.isSuccess());
        });
    }
    public static ChannelFuture closeChannel(Channel channel,Consumer<ChannelFuture> consumer){
        return closeChannel(channel).addListener((ChannelFuture cf)->{
            consumer.accept(cf);
        });
    }
    public static String parseChannelRemoteAddr(ChannelHandlerContext ctx){
        return parseChannelRemoteAddr(ctx.channel());
    }
    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }
}
