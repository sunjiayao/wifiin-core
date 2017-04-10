package com.wifiin.nio.netty;

public class NettyContext{
    private static NettyGeneralParams params;
    
    static void registerParams(NettyGeneralParams params){
        NettyContext.params=params;
    }
    public static NettyGeneralParams params(){
        return params;
    }
}
