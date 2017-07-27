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
    
    /**
     * 如果报文解析分好多步，本方法返回第一步的解析器
     * @return
     */
    protected abstract Decoder<I> decoder();
    @SuppressWarnings("unchecked")
    private Decoder<I> decoder=Decoder.FINAL_DECODER;
    private I i;
    /**
     * 解析报文
     * @param msg 待解析的字节保存在这里
     * @return 解析ByteBuf得到的报文对象
     */
    protected I decode(){
        if(decoder==Decoder.FINAL_DECODER){
            decoder=decoder();
        }
        try{
            do{
                buf.markReaderIndex();
                i=decoder.decode(i,buf);
                decoder=decoder.next();
            }while(decoder!=Decoder.FINAL_DECODER);
        }catch(Signal s){
            s.expect(ReplayingDecoderByteBuf.REPLAY);
            buf.resetReaderIndex();
        }finally{
            buf.terminate();
        }
        I r=i;
        i=null;
        return r;
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
        executor(i,(param)->{
            execute(ctx,param);
        });
    }
    private void execute(ChannelHandlerContext ctx,I i){
        O o=execute(i);
        if(o==OutputObject.CLOSE_CHANNEL){
            ctx.close();
        }else if(o!=OutputObject.ACCOMPLISHED){
            ByteBuf response=ctx.alloc().buffer();
            encode(o,response);
            ctx.writeAndFlush(response);
        }
    }
}
