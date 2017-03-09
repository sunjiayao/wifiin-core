package com.wifiin.springmvc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.wifiin.common.CommonConstant;
import com.wifiin.data.Bytes2StringType;
import com.wifiin.data.CompressType;
import com.wifiin.data.DataTransformerType;
import com.wifiin.data.EncryptionType;
import com.wifiin.data.SerializationType;
import com.wifiin.data.Transformer;
import com.wifiin.util.Help;
import com.wifiin.util.io.IOUtil;

/**
 * 本类能处理的Content-Type: wifiin/v+EncodeType。
 * eg. wifiin/4+2010102表示传输报文版本号是4（目前仅指加密KEY），并且传输数据经过转换JSON串、压缩、第四版AES加密，BASE64。
 * 2010102该值的意义为com.wifiin.data.*Type各枚举类型的相关值进行位运算后的16进制结果，从高位到低位分别是序列化、压缩、加密、BASE64，每一部分占一字节。
 * @author Running
 *
 */
@SuppressWarnings("rawtypes")
public class WifiinHttpMessageConverter extends AbstractGenericHttpMessageConverter{
    private static final Logger log=LoggerFactory.getLogger(WifiinHttpMessageConverter.class);
    
    private static final int CONVERTER_MASK=0xff;
    private static final char MEDIA_TYPE_SPLIRTOR='+';
    
    private MediaType requestMediaType;
    private MediaType responseMediaType;
    private Charset charset;
    
    private Map<String,Transformer> converters;

    public Map<String, Transformer> getConverters(){
        return converters;
    }
    public void setConverters(Map<String, Transformer> converters){
        this.converters=new ConcurrentHashMap<>();
        for(Map.Entry<String, Transformer> entry:converters.entrySet()){
            this.converters.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }

    public String getRequestMediaType(){
        return requestMediaType.toString();
    }

    public void setRequestMediaType(String mediaType){
        int idx=mediaType.indexOf(';');
        if(idx<0){
            this.requestMediaType = MediaType.parseMediaType(mediaType);
            charset=Charset.forName(CommonConstant.DEFAULT_CHARSET_NAME);
        }else{
            this.requestMediaType=MediaType.parseMediaType(mediaType.substring(0, idx).trim());
            charset=Charset.forName(mediaType.substring(idx+1).trim());
        }
    }
    public String getResponseMediaType(){
        return Help.convert(responseMediaType,requestMediaType).toString();
    }
    public void setResponseMediaType(String responseMediaType){
        int idx=responseMediaType.indexOf(';');
        if(idx<0){
            this.responseMediaType = MediaType.parseMediaType(responseMediaType);
        }else{
            this.responseMediaType = MediaType.parseMediaType(responseMediaType.substring(0, idx).trim());
        }
    }
    private int extractConverterType(int subType,int bitMove){
        return CONVERTER_MASK & (subType>>>(bitMove*8));
    }
    private String converterName(int converterType,int bitMove){
        return DataTransformerType.evalDataTransformerName(bitMove, converterType);
    }
    private int subType(String fullSubType,int subTypeSplitorIdx){
        return Integer.parseInt(fullSubType.substring(subTypeSplitorIdx+1),16);
    }
    private Object decode(String tag,String converterName,Object content,Class cls){
        Transformer transformer=converters.get(converterName);
        return transformer.decode(tag,content, cls);
    }
    private Object decode(String tag,int subType,int bitMove,Object content,Class cls){
        int convertType=extractConverterType(subType,bitMove);
        if(convertType>0){
            String converterName=converterName(convertType,bitMove);
            return decode(tag,converterName,content,cls);
        }else{
            return content;
        }
    }
    protected String extractFullSubType(){
        return SpringMVCContext.getQueryString();//mediaType.getSubtype();
    }
    protected Object readInternal(Type type,byte[] content,MediaType mediaType) throws IOException{
        String fullSubType=extractFullSubType();
        int idx=fullSubType.indexOf(MEDIA_TYPE_SPLIRTOR);
        String tag=fullSubType.substring(0, idx);//加密版本号
        int subType=subType(fullSubType,idx);
        //json compress encrypt base64
        int bitMove=0;
        Class cls=(Class)type;
        Object result=decode(tag,subType,bitMove++,content,cls);//BYTES2STRING_BYTES
               result=decode(tag,subType,bitMove++,result,cls);//DECRYPT
               result=decode(tag,subType,bitMove++,result,cls);//DECOMPRESS
               result=decode(tag,subType,bitMove++,result,cls);//SERIALIZE_BYTES
        SpringMVCContext.getRequest().setAttribute(SpringMVCConstant.REQUEST_PARAMS, result);
        return result;
    }
    @Override
    public Object read(Type type, Class contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException{
        MediaType mt=inputMessage.getHeaders().getContentType();
        InputStream in=inputMessage.getBody();
        byte[] content=IOUtil.read(in);
        return readInternal(type,content,mt);
    }
    private Object encode(String tag,String converterName,Object content){
        Transformer transformer=converters.get(converterName);
        return transformer.encode(tag,content);
    }
    private Object encode(String tag,int subType,int bitMove,Object content){
        int convertType=extractConverterType(subType,bitMove);
        if(convertType>0){
            String converterName=converterName(convertType,bitMove);
            return encode(tag,converterName,content);
        }else{
            return content;
        }
    }
    protected byte[] encode(Object t){
        int bitMove=3;
        String fullSubType=SpringMVCContext.getQueryString();//mediaType.getSubtype();
        int idx=fullSubType.indexOf(MEDIA_TYPE_SPLIRTOR);
        String tag=fullSubType.substring(0, idx);//加密版本号
        int subType=subType(fullSubType,idx);
        Result result=(Result)t;
        SpringMVCContext.getRequest().setAttribute(SpringMVCConstant.RESULT_FOR_LOG, result.getLog());
        t=encode(tag,subType,bitMove--,t);//SERIALIZE BYTES
        t=encode(tag,subType,bitMove--,t);//COMPRESS
        t=encode(tag,subType,bitMove--,t);//ENCRYPT
        byte[] content=(byte[])encode(tag,subType,bitMove--,t);//BYTES2STRING_BYTES
        return content;
    }
    @Override
    protected void writeInternal(Object t, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException{
        //json compress encrypt base64
//        MediaType mt=outputMessage.getHeaders().getContentType();
        byte[] content=encode(t);
        outputMessage.getBody().write(content);
    }

    @Override
    protected Object readInternal(Class clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException{
     // should not be called, since we override read instead
        throw new UnsupportedOperationException();
    }
    @Override
    protected boolean supports(Class cls){
        // should not be called, since we override canRead/Write instead
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean canRead(Type type, Class contextClass, MediaType mediaType) {
        if(log.isDebugEnabled()){
            log.debug("EncryptionHttpMessageConverter.canRead:"+mediaType+";"+this.requestMediaType);
        }
        if(this.requestMediaType!=null){
            return this.requestMediaType.includes(mediaType);
        }else{
            return false;
        }
    }

    @Override
    public boolean canWrite(Type type, Class contextClass, MediaType mediaType) {
        if(log.isDebugEnabled()){
            log.debug("EncryptionHttpMessageConverter.canWrite:"+mediaType+";"+this.responseMediaType);
        }
        if(this.responseMediaType!=null){
            return this.responseMediaType.includes(mediaType);
        }else{
            return false;
        }
    }
    
    public static void main(String[] args){
        int t=SerializationType.JSON_BYTES.getValue();
        t<<=8;
        t|=CompressType.GZIP.getValue();
        t<<=8;
        t|=EncryptionType.AES.getValue();
        t<<=8;
        t|=Bytes2StringType.BASE64_BYTES.getValue();
        System.out.println(t);
        System.out.println(Integer.toBinaryString(t));
        System.out.println("***"+Integer.toHexString(33620226));
    }
}
