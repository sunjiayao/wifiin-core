package com.wifiin.springmvc;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.wifiin.exception.CompressorException;
import com.wifiin.util.compress.GZIP;

@Component
public class GzipCompressor implements Compressor{

    @Override
    public byte[] compress(byte[] src){
        try{
            return GZIP.gzip(src);
        }catch(IOException e){
            throw new CompressorException(e);
        }
    }

    @Override
    public byte[] uncompress(byte[] src){
        try{
            return GZIP.ungzip(src);
        }catch(IOException e){
            throw new CompressorException(e);
        }
    }

    @Override
    public CompressorType compressorType(){
        return CompressorType.GZIP;
    }
    
}
