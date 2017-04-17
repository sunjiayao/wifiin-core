package com.wifiin.nio.netty.channel;

import com.wifiin.nio.netty.util.NettyUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * channel连接状态检查。start与end可以是异步的，会在两个线程执行。而且可能会有网络通讯，一般情况也应该是异步。
 * @author Running
 *
 */
public interface ChannelConnectChecker{
    /**
     * 开始检查
     * @param ch
     * @return
     */
    public boolean start(Channel ch);
    /**
     * 结束检查
     * @param ch
     */
    public default void end(Channel ch){}
    /**
     * 断开连接前做一些额外的工作
     * @param ch
     */
    public default void close0(Channel ch){}
    /**
     * 关闭channel
     * @param ch
     */
    public default void close(Channel ch){
        try{
            close0(ch);
            NettyUtil.closeChannel(ch);
        }finally{
            end(ch);
        }
    }
    /**
     * 关闭channel
     * @param ctx
     */
    public default void close(ChannelHandlerContext ctx){
        try{
            NettyUtil.closeChannel(ctx);
        }finally{
            end(ctx.channel());
        }
    }
}
