package com.wifiin.kv.netty;

import com.wifiin.kv.protocol.RequestDecoder;
import com.wifiin.kv.protocol.RequestMessage;
import com.wifiin.kv.protocol.ResponseEncoder;
import com.wifiin.nio.OutputObject;
import com.wifiin.nio.netty.channel.codec.AbstractCommonCodec;
import com.wifiin.nio.netty.channel.codec.Decoder;
import com.wifiin.nio.netty.channel.codec.ThreadLocalChannelHandlerContext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class RequestCodec extends AbstractCommonCodec<RequestMessage,OutputObject>{

    @Override
    protected Decoder<RequestMessage> decoder(){
        return RequestUUIDDecoder.instance;
    }

    @Override
    protected OutputObject execute(RequestMessage mssage){
        ChannelHandlerContext ctx=ThreadLocalChannelHandlerContext.get();
//        MultiThreadTaskRunner  TODO
        return OutputObject.ACCOMPLISHED;
    }

    @Override
    protected void encode(OutputObject o,ByteBuf buf){
        ResponseObject resp=(ResponseObject)o;
        ResponseEncoder.instance.encode(new ByteBufOutput(buf),resp.getUuid(),resp.getStatus(),resp.getBody());
    }
    
    public static class RequestUUIDDecoder implements Decoder<RequestMessage>{
        public static final RequestUUIDDecoder instance=new RequestUUIDDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            if(t==null){
                t=new RequestMessage();
            }
            RequestDecoder.decodeUUID(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestKeyLengthDecoder.instance;
        }
    }
    public static class RequestKeyLengthDecoder implements Decoder<RequestMessage>{
        public static final RequestKeyLengthDecoder instance=new RequestKeyLengthDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeKeyLength(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestKeyDecoder.instance;
        }
    }
    public static class RequestKeyDecoder implements Decoder<RequestMessage>{
        public static final RequestKeyDecoder instance=new RequestKeyDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeKey(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestDataTypeDecoder.instance;
        }
    }
    public static class RequestDataTypeDecoder implements Decoder<RequestMessage>{
        public static final RequestDataTypeDecoder instance=new RequestDataTypeDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeDataType(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestCMDDecoder.instance;
        }
    }
    public static class RequestCMDDecoder implements Decoder<RequestMessage>{
        public static final RequestCMDDecoder instance=new RequestCMDDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeCmd(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestBodyLengthDecoder.instance;
        }
    }
    public static class RequestBodyLengthDecoder implements Decoder<RequestMessage>{
        public static final RequestBodyLengthDecoder instance=new RequestBodyLengthDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeBodyLength(t,new ByteBufInput(buf));
            return t;
        }
        public Decoder<RequestMessage> next(){
            return RequestBodyDecoder.instance;
        }
    }
    public static class RequestBodyDecoder implements Decoder<RequestMessage>{
        public static final RequestBodyDecoder instance=new RequestBodyDecoder();
        @Override
        public RequestMessage decode(RequestMessage t,ByteBuf buf){
            RequestDecoder.decodeBody(t,new ByteBufInput(buf));
            return t;
        }
    }
}
