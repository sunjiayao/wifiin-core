package com.wifiin.nio.netty.channel.codec;

import io.netty.buffer.ByteBuf;

/**
 * 如果解析一个报文要分很多步，最后一定要以{@code FINAL_DECODER}结束。{@code FINAL_DECODER}不做任何事只是一个标记
 * @author Running
 *
 */
public interface Decoder<T>{
    public static final class FinalDecoder implements Decoder{
        public Object decode(Object t,ByteBuf buf){
            return null;
        }
        private static final FinalDecoder instance=new FinalDecoder();
    }
    public static final Decoder FINAL_DECODER=FinalDecoder.instance;
    public  T decode(T t,ByteBuf buf);
    
    public default Decoder<T> next(){
        return FinalDecoder.instance;
    }
}
