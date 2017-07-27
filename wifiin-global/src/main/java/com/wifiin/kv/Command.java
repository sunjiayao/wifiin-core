package com.wifiin.kv;

import com.wifiin.kv.store.Store;

public interface Command<R extends Result>{
    public R execute(byte[] uuid,Store store, byte[] key, byte... params);
    public int value();
}
