package com.wifiin.nio.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import com.wifiin.nio.netty.util.NettyUtil;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class ChannelWrapper implements Channel{
    private Channel ch;
    private InetSocketAddress addr;
    private Map<InetSocketAddress,Channel> channels;
    public ChannelWrapper(InetSocketAddress addr,Channel ch,Map<InetSocketAddress,Channel> channels){
        this.addr=addr;
        this.ch=ch;
        this.channels=channels;
    }
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key){
        return ch.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key){
        return ch.hasAttr(key);
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress){
        return ch.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress){
        return ch.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress,SocketAddress localAddress){
        return ch.connect(remoteAddress,localAddress);
    }

    @Override
    public ChannelFuture disconnect(){
        return ch.disconnect();
    }

    @Override
    public ChannelFuture close(){
        return NettyUtil.closeChannel(ch,(cf)->{
            this.channels.remove(addr);
        });
    }

    @Override
    public ChannelFuture deregister(){
        return ch.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress,ChannelPromise promise){
        return ch.bind(localAddress,promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress,ChannelPromise promise){
        return ch.connect(remoteAddress,promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress,SocketAddress localAddress,ChannelPromise promise){
        return ch.connect(remoteAddress,localAddress,promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise){
        return ch.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise){
        return ch.close(promise).addListener((f)->{
            this.channels.remove(addr);
        });
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise){
        return ch.deregister(promise);
    }

    @Override
    public ChannelFuture write(Object msg){
        return ch.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg,ChannelPromise promise){
        return ch.write(msg,promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg,ChannelPromise promise){
        return ch.writeAndFlush(msg,promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg){
        return ch.writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise(){
        return ch.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise(){
        return ch.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture(){
        return ch.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause){
        return ch.newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise(){
        return ch.voidPromise();
    }

    @Override
    public int compareTo(Channel o){
        return ch.compareTo(o);
    }

    @Override
    public ChannelId id(){
        return ch.id();
    }

    @Override
    public EventLoop eventLoop(){
        return ch.eventLoop();
    }

    @Override
    public Channel parent(){
        return ch.parent();
    }

    @Override
    public ChannelConfig config(){
        return ch.config();
    }

    @Override
    public boolean isOpen(){
        return ch.isOpen();
    }

    @Override
    public boolean isRegistered(){
        return ch.isRegistered();
    }

    @Override
    public boolean isActive(){
        return ch.isActive();
    }

    @Override
    public ChannelMetadata metadata(){
        return ch.metadata();
    }

    @Override
    public SocketAddress localAddress(){
        return ch.localAddress();
    }

    @Override
    public SocketAddress remoteAddress(){
        return ch.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture(){
        return ch.closeFuture();
    }

    @Override
    public boolean isWritable(){
        return ch.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable(){
        return ch.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable(){
        return ch.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe(){
        return ch.unsafe();
    }

    @Override
    public ChannelPipeline pipeline(){
        return ch.pipeline();
    }

    @Override
    public ByteBufAllocator alloc(){
        return ch.alloc();
    }

    @Override
    public Channel read(){
        return ch.read();
    }

    @Override
    public Channel flush(){
        return ch.flush();
    }
    
}
