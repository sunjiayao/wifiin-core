package com.wifiin.springmvc;

public interface Cryptor{
    public CryptoType crypto();
    public byte[] encrypt(String version,byte[] src);
    public byte[] decrypt(String version,byte[] src);
}
