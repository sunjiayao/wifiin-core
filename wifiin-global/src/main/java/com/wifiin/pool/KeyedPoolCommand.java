package com.wifiin.pool;

import java.util.function.BiFunction;

public interface KeyedPoolCommand<T> {
    public <R> R execute(String key,BiFunction<String,T,R> fn);
    public boolean returnKeyedObject(String key,T t);
    public T trieve(String key);
    public void shutdown();
}
