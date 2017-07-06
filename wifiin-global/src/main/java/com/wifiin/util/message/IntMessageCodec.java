package com.wifiin.util.message;

import com.wifiin.util.message.exception.BadMessageFormatException;
import com.wifiin.util.message.exception.TooLargeIntMessageException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class IntMessageCodec{
    private static final int MASK=0X80;
    public static int encode(long value,Output buf){
        return encode(value,buf,0);
    }
    public static int encode(long value,Output buf,boolean withLengthBytes){
        return encode(value,buf,withLengthBytes,0);
    }
    public static int encode(long value,Output buf,boolean withLengthBytes,int maxLengthBytes){
        if(withLengthBytes){
            int idx=buf.writerIndex();
            buf.writeByte(0);
            int bytes=encode(value,buf,maxLengthBytes);
            int end=buf.writerIndex();
            buf.setByte(idx,bytes);
            buf.writerIndex(end);
            return bytes;
        }
        return encode(value,buf);
    }
    /**
     * <pre>
     * do
     *    digit = X MOD 128
     *    X = X DIV 128
     *    // if there are more digits to encode, set the top bit of this digit
     *    if ( X > 0 )
     *      digit = digit OR 0x80
     *    endif
     *    'output' digit
     * while ( X> 0 )
     * </pre>
     * */
    public static int encode(long value,Output buf,int maxLengthBytes){
        long x=value;
        int loops=0;
        if(x<=0){
            buf.writeByte(0);
        }else{
            do{
                int digit = (int)(x % MASK);
                x /= 128;
                if ( x > 0 ){
                    digit |= MASK;
                }
                buf.writeByte(digit);
                loops++;
            }while(x>0 && overflow(loops,maxLengthBytes));
            if(x>0){
                buf.resetWriterIndex();
                throw new TooLargeIntMessageException(value, maxLengthBytes);
            }
        }
        return loops;
    }
    public static long decode(Input buf){
        return decode(buf,0);
    }
    public static long decode(Input buf,boolean withLengthBytes){
        return decode(buf,0,withLengthBytes);
    }
    public static long decode(Input buf,int maxLengthBytes,boolean withLengthBytes){
        int maxBytes=0;
        if(withLengthBytes){
            maxBytes=buf.readUnsignedByte();
        }
        if(maxBytes==0){
            return 0;
        }else if(maxBytes<0){
            throw new BadMessageFormatException("bytes of message value is less than zero");
        }else if(maxBytes>maxLengthBytes && maxLengthBytes>0){
            throw new TooLargeIntMessageException(maxLengthBytes);
        }
        return decode(buf,maxBytes);
    }
    /**
     * <pre>
     * multiplier = 1 
     *  value = 0 
     *  do 
     *    digit = 'next digit from stream'
     *    value += (digit AND 127) * multiplier 
     *    multiplier *= 128
     *  while ((digit AND 128) != 0)
     *  </pre>
     * @param buf
     */
    public static long decode(Input buf,int maxLengthBytes){
        long value = 0;
        long multiplier = 1;
        short digit;
        int loops = 0;
        do {
            digit = buf.readUnsignedByte();
            value += (digit & 127) * multiplier;
            multiplier *= MASK;
            loops++;
        } while ((digit & MASK) != 0 && overflow(loops,maxLengthBytes));
        if((digit & MASK)!=0){
            throw new TooLargeIntMessageException(maxLengthBytes);
        }
        return value;
    }
    private static boolean overflow(int loops,int maxLengthBytes){
        return maxLengthBytes<=0 || loops < maxLengthBytes;
    }
    
    public static void main(String[] args){
        class ByteBufOutput implements Output{
            ByteBuf buf;
            public ByteBufOutput(ByteBuf buf){
                this.buf=buf;
            }
            @Override
            public int writerIndex(){
                return buf.writerIndex();
            }
            @Override
            public void writerIndex(int index){
                buf.writerIndex(index);
            }
            @Override
            public void setByte(int index,int b){
                buf.setByte(index,b);
            }
            @Override
            public void markWriterIndex(){
                buf.markWriterIndex();
            }
            @Override
            public void writeByte(int value){
                buf.writeByte(value);
            }
            @Override
            public void writeBytes(byte[] buf){
                this.buf.writeBytes(buf);
            }
            @Override
            public void resetWriterIndex(){
                buf.resetWriterIndex();
            }
        }
        class ByteBufInput implements Input{
            ByteBuf buf;
            public ByteBufInput(ByteBuf buf){
                this.buf=buf;
            }
            @Override
            public short readUnsignedByte(){
                return buf.readUnsignedByte();
            }
            
        }
//        int i=-2;
        for(int i=0;i<Integer.MAX_VALUE;i++){
            ByteBuf buf=Unpooled.buffer();
            encode(i,new ByteBufOutput(buf));
            byte[] b=new byte[buf.readableBytes()];
            buf.readBytes(b);
            System.out.println(java.util.Arrays.toString(b));
        }
    }
}
