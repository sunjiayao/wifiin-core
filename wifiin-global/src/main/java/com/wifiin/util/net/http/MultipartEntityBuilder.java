package com.wifiin.util.net.http;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.ContentType;

public class MultipartEntityBuilder{
    private HttpClient http;
    private org.apache.http.entity.mime.MultipartEntityBuilder builder=org.apache.http.entity.mime.MultipartEntityBuilder.create();
    MultipartEntityBuilder(HttpClient http){
        this.http=http;
    }
    
    public MultipartEntityBuilder addTextPart(String name,String text){
        builder.addTextBody(name,text);
        return this;
    }
    public MultipartEntityBuilder addTextPart(String name,String text,ContentType contentType){
        builder.addTextBody(name,text,contentType);
        return this;
    }
    public MultipartEntityBuilder addTextPart(String name,String text,String contentType){
        return addTextPart(name,text,ContentType.parse(contentType));
    }
    public MultipartEntityBuilder addTextPart(String name,String text,String contentType,String charset){
        return addTextPart(name,text,ContentType.create(contentType,charset));
    }
    public MultipartEntityBuilder addTextPart(String name,String text,String contentType,Charset charset){
        return addTextPart(name,text,ContentType.create(contentType,charset));
    }
    
    public MultipartEntityBuilder addBinaryPart(String name,byte[] part){
        builder.addBinaryBody(name,part);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,File file){
        builder.addBinaryBody(name,file);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,InputStream in){
        builder.addBinaryBody(name,in);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,byte[] part,ContentType contentType,String filename){
        builder.addBinaryBody(name,part,contentType,filename);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,byte[] part,String contentType,String filename){
        return addBinaryPart(name,part,ContentType.parse(contentType),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,byte[] part,String contentType,String charset,String filename){
        return addBinaryPart(name,part,ContentType.create(contentType,charset),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,byte[] part,String contentType,Charset charset,String filename){
        return addBinaryPart(name,part,ContentType.create(contentType,charset),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,File file,ContentType contentType,String filename){
        builder.addBinaryBody(name,file,contentType,filename);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,File file,String contentType,String filename){
        return addBinaryPart(name,file,ContentType.parse(contentType),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,File file,String contentType,String charset,String filename){
        return addBinaryPart(name,file,ContentType.create(contentType,charset),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,File file,String contentType,Charset charset,String filename){
        return addBinaryPart(name,file,ContentType.create(contentType,charset),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,InputStream in,ContentType contentType,String filename){
        builder.addBinaryBody(name,in,contentType,filename);
        return this;
    }
    public MultipartEntityBuilder addBinaryPart(String name,InputStream in,String contentType,String filename){
        return addBinaryPart(name,in,ContentType.parse(contentType),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,InputStream in,String contentType,String charset,String filename){
        return addBinaryPart(name,in,ContentType.create(contentType,charset),filename);
    }
    public MultipartEntityBuilder addBinaryPart(String name,InputStream in,String contentType,Charset charset,String filename){
        return addBinaryPart(name,in,ContentType.create(contentType,charset),filename);
    }
    public HttpClient setRequestEntity(){
        http.setEntity(builder.build());
        return http;
    }
}
