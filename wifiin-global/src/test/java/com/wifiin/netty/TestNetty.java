package com.wifiin.netty;

import org.junit.Test;

import com.wifiin.nio.netty.NettyClient;
import com.wifiin.nio.netty.NettyClientParams;
import com.wifiin.nio.netty.NettyServer;
import com.wifiin.nio.netty.NettyServerParams;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;

public class TestNetty{
    @Test
    public void testSimpleInboundHandler() throws InterruptedException{
        new Thread(()->{
            NettyServerParams sparams=new NettyServerParams();
            sparams.addChannelHandlerClass(TestServerCodec.class);
            sparams.port(8080);
            new NettyServer(sparams).start();
        }).start();
        Thread.sleep(5000);
        NettyClientParams cparams=new NettyClientParams();
        cparams.addChannelHandlerClass(TestClientCodec.class);
        NettyClient client=new NettyClient(cparams).start();
        Channel channel=client.channel("localhost",8080);
        long now=System.currentTimeMillis();
        System.out.println("client:"+now);
        channel.writeAndFlush(Unpooled.buffer().writeLong(now));
        channel.flush();
        Thread.sleep(3000);
        System.exit(0);
    }
    
}
