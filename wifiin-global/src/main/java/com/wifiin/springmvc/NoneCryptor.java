package com.wifiin.springmvc;

import org.springframework.stereotype.Component;

@Component
public class NoneCryptor implements Cryptor{

    @Override
    public CryptoType crypto(){
        return CryptoType.NONE;
    }

    @Override
    public byte[] encrypt(String version,byte[] src){
        return src;
    }

    @Override
    public byte[] decrypt(String version,byte[] src){
        return src;
    }
    
}
