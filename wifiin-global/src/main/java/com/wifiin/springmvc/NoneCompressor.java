package com.wifiin.springmvc;

import org.springframework.stereotype.Component;

@Component
public class NoneCompressor implements Compressor{

    @Override
    public byte[] compress(byte[] src){
        return src;
    }

    @Override
    public byte[] uncompress(byte[] src){
        return src;
    }

    @Override
    public CompressorType compressorType(){
        return CompressorType.NONE;
    }
    
}
