package com.wifiin.test.kryo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wifiin.common.GlobalObject;

public class KryoUtilTest {
    @Test
    public void testKryoUtil() throws JsonProcessingException{
            Eight eight=new Eight();
            eight.setB(true);
            eight.setI(ThreadLocalRandom.current().nextInt());
            eight.setS(RandomStringUtils.random(256));
            String decimal=RandomStringUtils.random(128,"0123456789")+"."+RandomStringUtils.random(128,"0123456789");
            System.out.println(decimal);
            eight.setBd(new BigDecimal(decimal));
            eight.setBi(new BigInteger(RandomStringUtils.random(128,"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"),36));
            byte[] bytes=new byte[1024];
            ThreadLocalRandom.current().nextBytes(bytes);
            eight.setBs(bytes);
            eight.setD(ThreadLocalRandom.current().nextDouble());
            eight.setL(ThreadLocalRandom.current().nextLong());
            Nine nine=new Nine();
            nine.setDate(new Date());
            nine.setEight(eight);
            List<Nine> list=new ArrayList<>();
            list.add(nine);
            Kryo kryo=new Kryo();
            Output output=new Output(128,Integer.MAX_VALUE);
            kryo.writeClassAndObject(output, list);
            System.out.println(kryo.readClassAndObject(new Input(output.toBytes())));

            output=new Output(128,Integer.MAX_VALUE);
            kryo.writeObject(output, list);
//            System.out.println(output.toBytes().length);
            System.out.println(kryo.readObject(new Input(output.toBytes()),ArrayList.class));
//            KryoUtil kryo=KryoUtil.getInstance();
//            kryo.write(nine,output);
//            System.out.println("###############################################");
//            bytes=output.toBytes();
//            System.out.println(bytes.length);
//            nine=kryo.read(new Input(bytes), Nine.class);
//            System.out.println(GlobalObject.getJsonMapper().writeValueAsString(nine));
//            System.out.println("###############################################");
//            ByteBuf buf=kryo.writeByteBuf(nine);
//            System.out.println(buf.isDirect()+"    "+buf.writerIndex()+"   "+buf.toString());
//            nine=kryo.read(buf, Nine.class);
//            System.out.println(GlobalObject.getJsonMapper().writeValueAsString(nine));
    }
}
class One{
    private int i;

    public int getI(){
        return i;
    }

    public void setI(int i){
        this.i = i;
    }
    
}
class Two extends One{
    private String s;

    public String getS(){
        return s;
    }

    public void setS(String s){
        this.s = s;
    }
}
class Three extends Two{
    private boolean b;

    public boolean isB(){
        return b;
    }

    public void setB(boolean b){
        this.b = b;
    }
}
class Four extends Three{
    private long l;

    public long getL(){
        return l;
    }

    public void setL(long l){
        this.l = l;
    }
}
class Five extends Four{
    private byte[] bs;

    public byte[] getBs(){
        return bs;
    }

    public void setBs(byte[] bs){
        this.bs = bs;
    }
}
class Six extends Five{
    private double d;

    public double getD(){
        return d;
    }

    public void setD(double d){
        this.d = d;
    }
}
class Seven extends Six{
    private BigDecimal bd;

    public BigDecimal getBd(){
        return bd;
    }

    public void setBd(BigDecimal bd){
        this.bd = bd;
    }
}
class Eight extends Seven{
    private BigInteger bi;

    public BigInteger getBi(){
        return bi;
    }
    public void setBi(BigInteger bi){
        this.bi = bi;
    }
}
class Nine{
    private Date date;
    private Eight eight;

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public Eight getEight(){
        return eight;
    }

    public void setEight(Eight eight){
        this.eight = eight;
    }
}