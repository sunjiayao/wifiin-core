package com.wifiin.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.curator.shaded.com.google.common.collect.Lists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wifiin.common.exception.JsonException;

public class JSON{
    private static final ObjectMapper mapper=GlobalObject.getJsonMapper();
    public static String toJSON(Object o){
        try{
            return mapper.writeValueAsString(o);
        }catch(JsonProcessingException e){
            throw new JsonException(e);
        }
    }
    public static void toJSON(Object o,OutputStream out){
        try{
            mapper.writeValue(out,o);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static void toJSONList(List<?> src,Consumer<String> consumer){
        src.forEach((s)->{
            consumer.accept(toJSON(s));
        });
    }
    public static List<String> toJSONList(List<?> src){
        List<String> list=Lists.newArrayList();
        toJSONList(src,(s)->{
            list.add(s);
        });
        return list;
    }
    public static byte[] toJSONBytes(Object o){
        try{
            return mapper.writeValueAsBytes(o);
        }catch(JsonProcessingException e){
            throw new JsonException(e);
        }
    }
    public static void toJSON(Object o,File dest){
        try{
            mapper.writeValue(dest,o);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(String json,Class<T> t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> List<T> parse(List<String> src,Class<T> t){
        List<T> result=Lists.newArrayList();
        src.stream().map((s)->{
            return parse(s,t);
        }).forEach((i)->{
            result.add(i);
        });
        return result;
    }
    public static <T> T parse(byte[] json,Class<T> t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(File json,Class<T> t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> List<T> parseList(BufferedReader reader,Class<T> t){
        String line=null;
        try{
            List<T> list=Lists.newArrayList();
            while((line=reader.readLine())!=null){
                list.add(parse(line,t));
            }
            return list;
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> List<T> parseList(File json,Class<T> t){
        try(BufferedReader reader=new BufferedReader(new FileReader(json))){
            return parseList(reader,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(Reader reader,Class<T> t){
        try{
            return mapper.readValue(reader,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> List<T> parseList(Reader reader,Class<T> t){
        try(BufferedReader r=new BufferedReader(reader)){
            return parseList(r,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(InputStream in,Class<T> t){
        try{
            return mapper.readValue(in,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> List<T> parseList(InputStream in,Class<T> t){
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(in,CommonConstant.DEFAULT_CHARSET_INSTANCE))){
            return parseList(reader,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    private static <C extends Collection<?>> JavaType generateJavaType(Class<C> collectionType,Class<?>... elementTypes){
        return mapper.getTypeFactory().constructParametricType(collectionType, elementTypes);
    }
    public static <T> T parse(String json,JavaType t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(byte[] json,JavaType t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(File json,JavaType t){
        try{
            return mapper.readValue(json,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    public static <T> T parse(InputStream in,JavaType t){
        try{
            return mapper.readValue(in,t);
        }catch(IOException e){
            throw new JsonException(e);
        }
    }
    
    public static <C extends Collection<?>> C parse(String json,Class<C> collectionType,Class<?>... elementTypes){
        return parse(json,generateJavaType(collectionType,elementTypes));
    }
    public static <C extends Collection<?>> C parse(byte[] json,Class<C> collectionType,Class<?>... elementTypes){
        return parse(json,generateJavaType(collectionType,elementTypes));
    }
    public static <C extends Collection<?>> C parse(File json,Class<C> collectionType,Class<?>... elementTypes){
        return parse(json,generateJavaType(collectionType,elementTypes));
    }
    public static <C extends Collection<?>> C parse(InputStream in,Class<C> collectionType,Class<?>... elementTypes){
        return parse(in,generateJavaType(collectionType,elementTypes));
    }
}
