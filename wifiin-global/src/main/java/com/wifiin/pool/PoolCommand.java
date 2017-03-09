package com.wifiin.pool;

import java.util.function.Function;

public interface PoolCommand<T>{
    public <R> R execute(Function<T,R> fn);
    public T trieve();
    public boolean returnObject(T t);
    public void shutdown();
}
