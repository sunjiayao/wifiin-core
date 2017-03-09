package com.wifiin.common.cellphone.util;

import com.wifiin.exception.CellPhoneHomeException;
/**
 * 查号吧的response code是404，实际返回了内容，HttpClient不能处理
 * @author Running
 *
 */
public class CellPhoneChaHaoBa implements CellPhoneHome{

    @Override
    public String getURL(String phone){
        return "http://www.chahaoba.com/"+phone;
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        /*<li> 
         * 归属省份地区：
         * <a href="http://www.chahaoba.com/%E6%AD%A6%E6%B1%89" class="extiw" title="link:武汉">武汉</a>、
         * <a href="http://www.chahaoba.com/%E6%B9%96%E5%8C%97" class="extiw" title="link:湖北">湖北</a></li>*/
        int locationStart=response.indexOf("归属省份地区：");
        Object[] city=extractLocation(response,locationStart);
        if(city==null){
            throw new CellPhoneHomeException("could not find location of cellphone from the response "+response);
        }
        Object[] province=extractLocation(response,(Integer)city[1]);
        return new String[]{province!=null?(String)province[0]:"",(String)city[0]};
    }
    private Object[] extractLocation(String html,int fromIndex){
        String link="title=\"link:";
        int start=html.indexOf(link,fromIndex);
        if(start<0){
            return null;
        }
        start+=+link.length();
        int end=html.indexOf("\">",start);
        return new Object[]{html.substring(start,end),end};
    }
    
    public static void main(String[] args) throws Exception{
        System.out.println(java.util.Arrays.toString(new CellPhoneChaHaoBa().query("13241886176")));
    }
}
