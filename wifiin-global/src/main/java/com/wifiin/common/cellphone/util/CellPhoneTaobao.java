package com.wifiin.common.cellphone.util;

public class CellPhoneTaobao implements CellPhoneHome{

    @Override
    public String getURL(String phone){
        return "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel="+phone;
    }

    @Override
    public String[] parseResponse(String response)throws Exception{
        /*__GetZoneResult_ = {
    mts:'1850064',
    province:'北京',
    catName:'中国联通',
    telString:'18500640048',
    areaVid:'29400',
    ispVid:'137815084',
    carrier:'北京联通'
}*/
        String provinceKey="province:'";
        int idx=response.indexOf(provinceKey);
        int end=response.indexOf("',",idx);
        return new String[]{response.substring(idx+provinceKey.length(),end),""};
    }
    public static void main(String[] args){
        System.out.println(java.util.Arrays.toString(new CellPhoneTaobao().query("15841949186")));
    }
}
