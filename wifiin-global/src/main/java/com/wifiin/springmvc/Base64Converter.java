package com.wifiin.springmvc;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
@Component
public class Base64Converter implements MessageConverter{

    @Override
    public byte[] inputConvert(byte[] buf){
        return Base64.decodeBase64(buf);
    }

    @Override
    public byte[] outputConvert(byte[] buf){
        return Base64.encodeBase64(buf);
    }
    
}
