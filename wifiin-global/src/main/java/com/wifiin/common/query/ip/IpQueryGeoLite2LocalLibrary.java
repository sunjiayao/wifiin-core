package com.wifiin.common.query.ip;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.wifiin.common.query.RemoteQuery;
import com.wifiin.common.query.exception.QueryException;
import com.wifiin.util.ShutdownHookUtil;

/**
 * 数据库来源：https://dev.maxmind.com/zh-hans/geoip/geoip2/geolite2-%E5%BC%80%E6%BA%90%E6%95%B0%E6%8D%AE%E5%BA%93/
 * @author Running
 *
 */
public class IpQueryGeoLite2LocalLibrary implements RemoteQuery{
    private static final String IP_DAT="/GeoLite2-City.mmdb";
    private static final DatabaseReader db;
    static{
        try{
            File dbf=new File(IpQueryGeoLite2LocalLibrary.class.getResource(IP_DAT).toURI());
            db=new DatabaseReader.Builder(dbf).build();
            ShutdownHookUtil.addHook(()->{
                try{
                    db.close();
                }catch(IOException e){}
            });
        }catch(Exception e){
            throw new QueryException(e);
        }
    }
    @Override
    public String getURL(String param){
        //useless
        return null;
    }
    @Override
    public String[] parseResponse(String response) throws Exception{
        return find(response);
    }
    public static String[] find(String ip){
        try{
            InetAddress addr=InetAddress.getByName(ip);
            CityResponse resp=db.city(addr);
            return new String[]{resp.getCountry().getName(),"",resp.getCity().getName()};
        }catch(Exception e){
            throw new QueryException(e);
        }
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(find("210.75.225.254")));
    }
}
