package com.wifiin.data;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.wifiin.common.CommonConstant;
import com.wifiin.data.exception.DataTransformerException;
import com.wifiin.util.Help;
import com.wifiin.util.security.EnhancedSymmetricEncryptor;

public abstract class AESTransformer<T> implements Transformer<T,byte[],byte[]>{
    
    private String charset;
    
    public String getCharset(){
        return Help.convert(charset,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    public void setCharset(String charset){
        this.charset = charset;
    }

    public abstract AESParams getAESParams(T t);
    
    private EnhancedSymmetricEncryptor getAES(T t) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
        AESParams params=getAESParams(t);
        return EnhancedSymmetricEncryptor.getAES(params.getKey(),params.getSalt(),params.getIv(),getCharset());
    }
    @Override
    public byte[] encode(T t,byte[] p){
        try{
            return getAES(t).encrypt(p);
        }catch(Exception e){
            throw new DataTransformerException(e);
        }
    }

    @Override
    public byte[] decode(T t,byte[] r, Class<byte[]> cls){
        try{
            return getAES(t).decrypt(r);
        }catch(Exception e){
            throw new DataTransformerException(e);
        }
    }

}
