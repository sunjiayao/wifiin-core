package com.wifiin.common.cellphone.util;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;

public class CellPhoneDaHanBank implements CellPhoneHome{
    private static final String PROVINCE_ID="provinceID";
    private static final Map<Integer,String> PROVINCE_MAP;
    static{
        Map map=Maps.newHashMap();
        PROVINCE_MAP=map;
        map.put(0 ,"全国");
        map.put(1 ,"北京");
        map.put(2 ,"新疆");
        map.put(3 ,"重庆");
        map.put(4 ,"广东");
        map.put(5 ,"浙江");
        map.put(6 ,"天津");
        map.put(7 ,"广西");
        map.put(8 ,"内蒙古");
        map.put(9 ,"宁夏");
        map.put(10,"江西");
        map.put(11,"安徽");
        map.put(12,"贵州");
        map.put(13,"陕西");
        map.put(14,"辽宁");
        map.put(15,"山西");
        map.put(16,"青海");
        map.put(17,"四川");
        map.put(18,"江苏");
        map.put(19,"河北");
        map.put(20,"西藏");
        map.put(21,"福建");
        map.put(22,"吉林");
        map.put(23,"云南");
        map.put(24,"上海");
        map.put(25,"湖北");
        map.put(26,"海南");
        map.put(27,"甘肃");
        map.put(28,"湖南");
        map.put(29,"山东");
        map.put(30,"河南");
        map.put(31,"黑龙江");
        map.put(32,"未知");
    }
    @Override
    public String getURL(String phone){
        return "http://if.dahanbank.cn/FCGetAttribution?mobile="+phone;
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        return new String[]{PROVINCE_MAP.get(GlobalObject.getJsonMapper().readValue(response,Map.class).get(PROVINCE_ID)),""};
    }
    public static void main(String[] args) throws Exception{
        System.out.println(java.util.Arrays.toString(new CellPhoneDaHanBank().query("13241886176")));
    }
}
