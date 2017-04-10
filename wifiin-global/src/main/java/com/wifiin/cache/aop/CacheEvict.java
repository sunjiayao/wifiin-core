package com.wifiin.cache.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
@Inherited  
@Documented
public @interface CacheEvict{
    String[] keyPattern();
    KeyParams keyParams();
    int[] cacheKeyArgs() default {};
    boolean heapCache();
}
