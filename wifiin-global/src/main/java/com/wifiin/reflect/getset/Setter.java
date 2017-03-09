package com.wifiin.reflect.getset;

public interface Setter<O,V>{
    public void set(O t,V v);
    public Class<V> propertyType();
}
