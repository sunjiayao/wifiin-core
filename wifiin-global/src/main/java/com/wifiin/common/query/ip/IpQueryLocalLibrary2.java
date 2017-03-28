package com.wifiin.common.query.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.wifiin.common.query.RemoteQuery;

/**
 * copied is from https:{@link //github.com/17mon/java}
 * ip library is from {@link https://www.ipip.net/download.html} {@link https://www.ipip.net/free_download/}
 * @author Running
 *
 */
public class IpQueryLocalLibrary2 implements RemoteQuery{
    
    public IpQueryLocalLibrary2(){
        load();
    }
    
    @Override
    public String getURL(String param){
        //useless
        return null;
    }
    
    public String request(String param) throws KeyManagementException,NoSuchAlgorithmException,IOException{
        return param;
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        return find(response);
    }
    
    public static boolean enableFileWatch = false;

    private static int offset;
    private static int indexLength;
    private static int[] index = new int[256];
    private static byte[] dataBuf;
    private static byte[] indexBuf;

    public static String[] find(String ip) {
        int ipPrefix = Integer.parseInt(ip.substring(0, ip.indexOf(".")));
        long ipLong  = ip2long(ip);
        int start = index[ipPrefix];
        int maxCompLen = offset - 1028;
        long indexOffset = -1;
        int indexLength = -1;
        byte b = 0;
        ByteBuffer indexBuffer=ByteBuffer.wrap(indexBuf);
        for (start = start * 8 + 1024; start < maxCompLen; start += 8) {
            if (int2long(indexBuffer.getInt(start)) >= ipLong) {
                indexOffset = bytesToLong(b, indexBuffer.get(start + 6), indexBuffer.get(start + 5), indexBuffer.get(start + 4));
                indexLength = 0xFF & indexBuffer.get(start + 7);
                break;
            }
        }
        return parseLine(offset + (int) indexOffset - 1024,indexLength);
    }
    private static String[] parseLine(int offset,int len){
        String info=new String(dataBuf,offset,len, StandardCharsets.UTF_8);
        String[] result=new String[3];
        int start=0;
        int end=info.indexOf('\t');
        for(int i=0,l=result.length;i<l;i++){
            result[i]=info.substring(start,end>0?end:info.length());
            start=end+1;
            end=info.indexOf('\t',start);
        }
        return result;
    }
    private static void readDat() throws IOException{
        try(InputStream in=IpQueryLocalLibrary2.class.getResourceAsStream("/ip.dat");
        ByteArrayOutputStream out=new ByteArrayOutputStream();){
            byte[] buf=new byte[1024];
            int len=0;
            while((len=in.read(buf)) != -1){
                out.write(buf,0,len);
            }
            in.close();
            dataBuf = out.toByteArray();
        }
    }
    private static void load() {
        try {
            readDat();
            ByteBuffer dataBuffer = ByteBuffer.wrap(dataBuf);
            dataBuffer.position(0);
            indexLength = dataBuffer.getInt();
            byte[] indexBytes = new byte[indexLength];
            dataBuffer.get(indexBytes, 0, indexLength - 4);
            ByteBuffer indexBuffer = ByteBuffer.wrap(indexBytes);
            indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            offset = indexLength;

            int loop = 0;
            while (loop++ < 256) {
                index[loop - 1] = indexBuffer.getInt();
            }
            indexBuffer.order(ByteOrder.BIG_ENDIAN);
            indexBuf=indexBuffer.array();
            dataBuffer.get(dataBuf,offset,dataBuffer.remaining());
        } catch (IOException ioe) {} finally {}
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return ((a & 0xffL) << 24) | ((b & 0xffL) << 16) | ((c & 0xffL) << 8) | (d & 0xffL);
    }


    private static long ip2long(String ip)  {
        long result=0;
        int start=0;
        int end=ip.indexOf('.');
        for(int i=0;i<4;i++){
            result=(result<<8)|Long.parseLong(ip.substring(start,end>0?end:ip.length()));
            start=end+1;
            end=ip.indexOf('.',start);
        }
        return result;
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
    public static void main(String[] args){
        String[] ss = "210.75.225.254".split("\\.");
        long a, b, c, d;
        a = Integer.parseInt(ss[0]);
        b = Integer.parseInt(ss[1]);
        c = Integer.parseInt(ss[2]);
        d = Integer.parseInt(ss[3]);
        System.out.println((a << 24) | (b << 16) | (c << 8) | d);
        System.out.println(ip2long("210.75.225.254"));
        System.out.println(bytesToLong((byte)210,(byte)75,(byte)225,(byte)254));
        load();
        System.out.println(java.util.Arrays.toString(find("210.75.225.254")));
    }
}
