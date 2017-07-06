package com.wifiin.kv.store;

public interface StoreIterator{
    public void close();
    public boolean valid();
    public void next();
    public void prev();
    public byte[] key();
    public byte[] value();
}