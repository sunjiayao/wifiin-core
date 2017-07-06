package com.wifiin.springmvc;

import org.springframework.stereotype.Component;

@Component
public class NoneConverter implements MessageConverter{

    @Override
    public byte[] inputConvert(byte[] buf){
        return buf;
    }

    @Override
    public byte[] outputConvert(byte[] buf){
        return buf;
    }
    
}
