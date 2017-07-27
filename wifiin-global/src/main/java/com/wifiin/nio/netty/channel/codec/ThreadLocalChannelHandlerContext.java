package com.wifiin.nio.netty.channel.codec;

import io.netty.channel.ChannelHandlerContext;

public class ThreadLocalChannelHandlerContext{
    private static final ThreadLocal<ChannelHandlerContext> ctx=new ThreadLocal<>();
    public static ChannelHandlerContext get(){
        return ctx.get();
    }
    public static void set(ChannelHandlerContext ctx){
        ThreadLocalChannelHandlerContext.ctx.set(ctx);
    }
}
