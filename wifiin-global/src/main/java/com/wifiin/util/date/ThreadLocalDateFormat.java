package com.wifiin.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.util.Help;

public class ThreadLocalDateFormat{
    private static final ThreadLocal<Map<String,DateFormat>> dateFormatMap=new ThreadLocal<>();
    private static DateFormat getFormat(String format){
        Map<String,DateFormat> map=dateFormatMap.get();
        if(map==null){
            map=Maps.newHashMap();
            dateFormatMap.set(map);
        }
        format=format.intern();
        return map.computeIfAbsent(format,(f)->{
            return new SimpleDateFormat(f);
        });
    }
    public static String format(Date date,String format){
        return date==null?null:getFormat(format).format(date);
    }
    public static Date parse(String src,String format) throws ParseException{
        return Help.isEmpty(src)?null:getFormat(format).parse(src);
    }
}
