package com.wifiin.netty;

import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.channel.codec.Decoder;

import io.netty.buffer.ByteBuf;

public class TestClientCodec extends AbstractCommonCodec<TestDataWrapper,OutputObject>{


    @Override
    protected void executor(TestDataWrapper i,Consumer<TestDataWrapper> consumer){
        consumer.accept(i);
    }

    @Override
    protected OutputObject execute(TestDataWrapper i){
        try{
            System.out.println(GlobalObject.getJsonMapper().writeValueAsString(i));
        }catch(JsonProcessingException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return OutputObject.ACCOMPLISHED;
    }

    @Override
    protected void encode(OutputObject o,ByteBuf buf){
        // TODO Auto-generated method stub
        System.out.println("cannot execute here");
        throw new RuntimeException("cannot execute here");
    }

    @Override
    protected Decoder<TestDataWrapper> decoder(){
        return new Decoder<TestDataWrapper>(){
            @Override
            public TestDataWrapper decode(TestDataWrapper t,ByteBuf buf){
                int len=buf.readInt();
                return new TestDataWrapper(len);
            }
            public Decoder<TestDataWrapper> next(){
                return new Decoder<TestDataWrapper>(){
                    @Override
                    public TestDataWrapper decode(TestDataWrapper t,ByteBuf buf){
                        TestDataWrapper w=(TestDataWrapper)t;
                        byte[] bytes=new byte[w.len];
                        buf.readBytes(bytes,0,w.len);
                        w.data=(TestOutputObject)GlobalObject.getFSTConfiguration().asObject(bytes);
                        return w;
                    }
                };
            }
        };
    }
}
