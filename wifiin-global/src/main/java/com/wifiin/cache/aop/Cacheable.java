package com.wifiin.cache.aop;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable{
    String keyPattern();
    int expire();
    boolean heapCache();
    CachedDataFormat format();
}
