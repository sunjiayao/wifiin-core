package com.wifiin.kv;

import com.wifiin.kv.store.Store;

public interface Command<R extends Result>{
    public R execute(Store store, byte[] key,int offset, byte... params);
}
