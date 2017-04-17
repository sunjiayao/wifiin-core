package com.wifiin.netty;

import java.util.function.Consumer;

import com.wifiin.common.GlobalObject;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

public class TestServerCodec extends AbstractCommonCodec<Long,TestOutputObject>{

    @Override
    protected void executor(Long i,Consumer<Long> consumer){
        consumer.accept(i);
    }

    @Override
    protected TestOutputObject execute(Long i){
        System.out.println("server:"+i);
        return new TestOutputObject(System.currentTimeMillis());
    }

    @Override
    protected void encode(TestOutputObject o,ByteBuf buf){
        byte[] bytes=GlobalObject.getFSTConfiguration().asByteArray(o);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    protected Decoder<Long> decoder(){
        return new Decoder(){
            @Override
            public Object decode(Object t,ByteBuf buf){
                return buf.readLong();
            }
        };
    }
    
}
