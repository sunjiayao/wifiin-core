package com.wifiin.nio.netty;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.wifiin.log.LoggerFactory;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.exception.NioException;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.util.NettyUtil;
import com.wifiin.util.MachineUtil;
import com.wifiin.util.ShutdownHookUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyServer<I,O extends OutputObject,T extends AbstractCommonCodec<I,O>>{
    private static final Logger log=LoggerFactory.getLogger(NettyUtil.NETTY_LOG_NAME);
    private static final String CHANNEL_EVENT_LOOP_GROUP_NAME="NettyServerChannelThread";
    private static final String ACCEPTOR_EVENT_LOOP_GROUP_NAME="NettyServerAcceptorThread";
    private static final String SELECTOR_EVENT_LOOP_GROUP_NAME="NettyServerSelectorThread";
    private ServerBootstrap bootstrap=new ServerBootstrap();
    private List<DefaultEventExecutorGroup> channelEventLoopGroups;
    private EventLoopGroup acceptorEventLoopGroup;
    private EventLoopGroup selectorEventLoopGroup;
    private NettyServerParams<I,O,T> params;
    private Channel channel;
    public NettyServer(NettyServerParams<I,O,T> params){
        channelEventLoopGroups=Lists.newArrayList();
        acceptorEventLoopGroup=params.newAcceptorEventLoopGroup(ACCEPTOR_EVENT_LOOP_GROUP_NAME);
        selectorEventLoopGroup=params.newEventLoopGroup(SELECTOR_EVENT_LOOP_GROUP_NAME);
        this.params=params;
        ShutdownHookUtil.addHook(()->{
            shutdown();
        });
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public void start(){
            bootstrap.group(acceptorEventLoopGroup, selectorEventLoopGroup);
            bootstrap.channel(MachineUtil.isLinux()?EpollServerSocketChannel.class:NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                     .option(ChannelOption.SO_REUSEADDR, true)
                     .option(ChannelOption.SO_KEEPALIVE, true)
                     .childOption(ChannelOption.TCP_NODELAY, true);
            for(Map.Entry<ChannelOption,Object> option:params.channelOptions().entrySet()){
                bootstrap.option(option.getKey(),option.getValue());
            }
            params.childChannelOptions().entrySet().forEach((option)->{
                bootstrap.childOption(option.getKey(),option.getValue());
            });
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                     .localAddress(params.port())
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                          @Override
                          public void initChannel(SocketChannel ch) throws Exception {
                              ChannelHandler[] channelHandlers=params.channelHandlers();
                              for(int i=0,l=channelHandlers.length;i<l;i++){
                                  DefaultEventExecutorGroup group=params.newEventExecutorGroup(CHANNEL_EVENT_LOOP_GROUP_NAME);
                                  channelEventLoopGroups.add(group);
                                  ch.pipeline()
                                    .addLast(group,
                                            new IdleStateHandler(0, 0, params.maxIdleSeconds()),
                                            new NettyConnectManageHandler("SERVER",params.channelConnectChecker()),
                                            channelHandlers[channelHandlers.length-1]);
                              }
                          }
                     });
        try {
            channel=bootstrap.bind().sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e1) {
            throw new NioException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
    }
    public void shutdown(){
        acceptorEventLoopGroup.shutdownGracefully();
        selectorEventLoopGroup.shutdownGracefully();
        channelEventLoopGroups.forEach((g)->{
            g.shutdownGracefully();
        });
        channel.close();
    }
}
