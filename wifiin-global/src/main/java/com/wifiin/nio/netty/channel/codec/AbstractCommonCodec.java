package com.wifiin.nio.netty.channel.codec;

import java.util.function.Consumer;

import com.wifiin.nio.OutputObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Signal;

/**
 * 异步化处理用netty从对端接收到的数据，并向对端发送数据。本类拥有成员属性，不能是{@code @Sharable}。
 * @author Running
 *
 * @param <I> 接收到的对端数据将解析成这个类的对象
 * @param <O> 业务处理结果对象
 */
public abstract class AbstractCommonCodec<I,O extends OutputObject> extends SimpleChannelInboundHandler<ByteBuf>{

    private ReplayingDecoderByteBuf buf=new ReplayingDecoderByteBuf();
    public AbstractCommonCodec(){}
    private class Decoded{
        public Decoder<I> decoder;
        public I i;
    }
    /**
     * 如果报文解析分好多步，本方法返回第一步的解析器
     * @return
     */
    protected abstract Decoder<I> decoder();
    private ThreadLocal<Decoded> decoded=new ThreadLocal<>();
    
    /**
     * 解析报文
     * @param msg 待解析的字节保存在这里
     * @return 解析ByteBuf得到的报文对象
     */
    protected I decode(){
        Decoded decoded=this.decoded.get();
        if(decoded==null){
            decoded=new Decoded();
            this.decoded.set(decoded);
        }
        Decoder<I> decoder=decoded.decoder;
        I i=decoded.i;
        if(decoder==null || decoder==Decoder.FINAL_DECODER){
            decoder=decoder();
            decoded.decoder=decoder;
        }
        try{
            do{
                buf.markReaderIndex();
                i=decoder.decode(i,buf);
                decoder=decoder.next();
                decoded.decoder=decoder;
                decoded.i=i;
            }while(decoder!=Decoder.FINAL_DECODER);
        }catch(Signal s){
            s.expect(ReplayingDecoderByteBuf.REPLAY);
            buf.resetReaderIndex();
            buf.discardReadBytes();
        }finally{
            buf.terminate();
        }
        decoded.i=null;
        return i;
    }
    /**
     * 最好将程序异步化执行。本类对象会将execute(I) encode(O,ByteBuf)放到一个Consumer对象里作为本方法参数执行。
     * @param i 接收到的对端数据
     * @param consumer 封装了execute(I)和encode(O,ByteBuf)的对象，本参数的参数就是i
     */
    protected void executor(I i,Consumer<I> consumer){
        consumer.accept(i);
    }
    /**
     * 业务逻辑在这里
     * @param i 解析得到的报文对象
     * @param channelId channel的惟一标识，来自Channel.id().asLongText();
     * @return 业务处理结果，如果返回值是OuterObject.ACCOMPLISHED表示不需要将处理结果写到对端，程序将忽略这个结果
     */
    protected abstract O execute(I i);
    /**
     * 报文对象序列化成ByteBuf
     * @param o   待序列化的对象
     * @param buf 序列化后的字节存在这里
     */
    protected abstract void encode(O o,ByteBuf buf);
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx,ByteBuf msg) throws Exception{
        buf.setCumulation(msg);
        ThreadLocalChannelHandlerContext.set(ctx);
        I i=decode();
        msg.release();
        executor(i,(param)->{
            execute(ctx,param);
        });
    }
    protected void execute(ChannelHandlerContext ctx,I i){
        O o=execute(i);
        if(o==OutputObject.CLOSE_CHANNEL){
            ctx.close();
        }else if(o!=OutputObject.ACCOMPLISHED){
            ByteBuf response=null;
            if(!(o instanceof ByteBuf)){
                response=(ByteBuf)o;
            }else{
                response=ctx.alloc().buffer();
                encode(o,response);
            }
            ctx.writeAndFlush(response);
        }
    }
}
