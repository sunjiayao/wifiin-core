package com.wifiin.springmvc;

public interface Compressor{
    public CompressorType compressorType();
    public byte[] compress(byte[] src);
    public byte[] uncompress(byte[] src);
}
