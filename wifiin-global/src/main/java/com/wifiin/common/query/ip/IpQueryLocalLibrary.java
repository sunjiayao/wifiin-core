package com.wifiin.common.query.ip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.wifiin.common.query.RemoteQuery;

/**
 * copied is from https:{@link //github.com/17mon/java}
 * ip library is from {@link https://www.ipip.net/download.html} {@link https://www.ipip.net/free_download/}
 * @author Running
 *
 */
public class IpQueryLocalLibrary implements RemoteQuery{
    
    public IpQueryLocalLibrary(){}
    
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
    private static final String IP_DAT="/ip.dat";
    private static final TreeMap<Long,String[]> IP_MAP=Maps.newTreeMap();
    static{
        long start=System.currentTimeMillis();
        load();
        System.out.println("consumed:"+(System.currentTimeMillis()-start));
    }
    public static String[] find(String ip){
        return IP_MAP.tailMap(ip2long(ip),true).firstEntry().getValue();
    }
    private static void populateMap(int offset,int[] index,byte[] dataBuf,byte[] indexBuf) {
        Map<Long,String[]> map=Maps.newHashMap();
        for(int ipPrefix=0,l=index.length;ipPrefix<l;ipPrefix++){
            int start = index[ipPrefix];
            int maxCompLen = offset - 1028;
            ByteBuffer indexBuffer=ByteBuffer.wrap(indexBuf);
            for (start = start * 8 + 1024; start < maxCompLen; start += 8) {
                byte b = 0;
                long k=int2long(indexBuffer.getInt(start));
                long indexOffset = bytesToLong(b, indexBuffer.get(start + 6), indexBuffer.get(start + 5), indexBuffer.get(start + 4));
                int indexLength = 0xFF & indexBuffer.get(start + 7);
                String[] location=parseLine(offset + (int) indexOffset - 1024,indexLength,dataBuf);
                map.put(k,location);
            }
        }
        synchronized(IP_MAP){
            IP_MAP.putAll(map);
        }
    }
    private static String[] parseLine(int offset,int len,byte[] dataBuf){
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
    private static byte[] readDat() throws IOException{
        try(InputStream in=IpQueryLocalLibrary.class.getResourceAsStream(IP_DAT);
        ByteArrayOutputStream out=new ByteArrayOutputStream();){
            byte[] buf=new byte[1024];
            int len=0;
            while((len=in.read(buf)) != -1){
                out.write(buf,0,len);
            }
            in.close();
            return out.toByteArray();
        }
    }
    private static void load(){
        try {
            byte[] dataBuf=readDat();
            ByteBuffer dataBuffer = ByteBuffer.wrap(dataBuf);
            dataBuffer.position(0);
            int indexLength = dataBuffer.getInt();
            byte[] indexBytes = new byte[indexLength];
            dataBuffer.get(indexBytes, 0, indexLength - 4);
            ByteBuffer indexBuffer = ByteBuffer.wrap(indexBytes);
            indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int offset = indexLength;
            int[] index=new int[256];
            int loop = 0;
            while (loop++ < 256) {
                index[loop - 1] = indexBuffer.getInt();
            }
            indexBuffer.order(ByteOrder.BIG_ENDIAN);
            byte[] indexBuf=indexBuffer.array();
            dataBuffer.get(dataBuf,offset,dataBuffer.remaining());
            populateMap(offset,index,dataBuf,indexBuf);
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
        System.out.println(java.util.Arrays.toString(find("0.0.0.0")));
        System.out.println(java.util.Arrays.toString(find("255.255.255.255")));
        System.out.println(java.util.Arrays.toString(find("210.75.225.254")));
        System.out.println(IP_MAP.size());
    }
}
