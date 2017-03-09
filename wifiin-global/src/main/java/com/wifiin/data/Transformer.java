package com.wifiin.data;

public interface Transformer<T,P,R> {
    public R encode(T t,P p);
    public P decode(T t,R r,Class<P> cls);
}
