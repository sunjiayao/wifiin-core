package com.wifiin.nio.netty;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.log.LoggerFactory;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.util.NettyUtil;
import com.wifiin.util.MachineUtil;
import com.wifiin.util.ShutdownHookUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyClient<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>>{
    private static final Logger log = LoggerFactory.getLogger(NettyUtil.NETTY_LOG_NAME);
    private static final String NETTY_CLIENT_EVENT_LOOP_GROUP_NAME="NettyClientSelectorThread";
    private static final String NETTY_CLIENT_EVENT_EXECUTOR_GROUP_NAME="NettyClientEventExecutorThread";

    private final Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup eventLoopGroupWorker;
    private List<DefaultEventExecutorGroup> channelEventLoopGroups;
    private NettyClientParams<I,O,T> params;
    private Map<InetSocketAddress,Channel> channels=Maps.newConcurrentMap();
    public NettyClient(NettyClientParams<I,O,T> params){
        eventLoopGroupWorker=params.newEventLoopGroup(NETTY_CLIENT_EVENT_LOOP_GROUP_NAME);
        this.params=params;
        channelEventLoopGroups=Lists.newArrayList();
        ShutdownHookUtil.addHook(()->{
            shutdown();
        });
    }
    
    @SuppressWarnings({"unchecked","rawtypes"})
    public NettyClient<I,O,T> start() {
        bootstrap.group(this.eventLoopGroupWorker).channel(MachineUtil.isLinux()?EpollSocketChannel.class:NioSocketChannel.class)//
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true);
        for(Map.Entry<ChannelOption,Object> option:params.channelOptions().entrySet()){
            bootstrap.option(option.getKey(),option.getValue());
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelHandler[] channelHandlers=params.channelHandlers();
                for(int i=0,l=channelHandlers.length;i<l;i++){
                    DefaultEventExecutorGroup group=params.newEventExecutorGroup(NETTY_CLIENT_EVENT_EXECUTOR_GROUP_NAME);
                    channelEventLoopGroups.add(group);
                    ch.pipeline()
                      .addLast(group,
                               new IdleStateHandler(0, 0, params.maxIdleSeconds()),
                               new NettyConnectManageHandler("CLIENT",params.channelConnectChecker()),
                               channelHandlers[i]);
                }
            }
        });
        return this;
    }
    public Channel channel(){
        return channel(params.host(),params.port());
    }
    public Channel channel(String host){
        return channel(host,params.port());
    }
    public Channel channel(int port){
        return channel(params.host(),port);
    }
    public Channel channel(String host,int port){
        return channel(InetSocketAddress.createUnresolved(host,port));
    }
    public Channel channel(InetSocketAddress addr){
        Channel ch=channels.get(addr);
        if(channelOK(ch)){
            return ch;
        }else{
            closeChannel(ch);
        }
        return channels.computeIfAbsent(addr,(ad)->{
            long timeout=params.connectionTimeoutMillis();
            ChannelFuture cf=bootstrap.connect(ad);
//                                      .syncUninterruptibly();
            if(cf.awaitUninterruptibly(timeout) && channelOK(cf.channel())){
                log.info("createChannel: connect remote host[{}] success, {}", ad, cf.toString());
                return new ChannelWrapper(addr,cf.channel(),channels);
            }else{
                log.warn("createChannel: connect remote host[" + ad + "] failed, " + cf.toString(), cf.cause());
                closeChannel(cf.channel());
                return null;
            }
        });
    }
    private boolean channelOK(Channel ch){
        return ch!=null && ch.isActive();
    }
    
    public void shutdown(){
        eventLoopGroupWorker.shutdownGracefully();
        channelEventLoopGroups.forEach((g)->{
            g.shutdownGracefully();
        });
        channels.values().forEach((ch)->{
            closeChannel(ch);
        });
        channels.clear();
    }
    private void closeChannel(Channel ch){
        if(ch==null){
            return;
        }
        try{
            NettyUtil.closeChannel(ch);
        }catch(Exception e){}
    }
}
