package com.wifiin.cache.aop;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 使用本注解的方法会缓存方法返回值。<br/>
 * keyPattern是缓存key模板，使用{@code TextTemplateFormatterFactory.getPlainTextTemplateFormatter(pattern)}格式化key。<br/>
 * expire是缓存超时时间，默认是3600秒，如果expire值是0就采用默认值<br/>
 * heapCache表示是否使用堆缓存，堆缓存寿命统一是5秒<br/>
 * cacheKeyArgs是缓存key的参数，如果不指定所有方法参数都可以用来格式化缓存key，如果指定了会使用指定的参数格式化缓存key<br/>
 * 作为缓存key的格式化参数不能复合对象和基本类型对象混用。复合对象包括自定义对象、map、list、set，基本类型对象包括字符串、基本类型的包装类对象<br/>
 * format是缓存值的数据格式。默认是调用对象的toString()方法作为缓存的内容。堆缓存保存原始对象，不格式化。
 * @author Running
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable{
    String keyPattern() default "";
    int expire() default 0;
    CacheType cacheType() default CacheType.REDIS;
    int[] cacheKeyArgs() default {};
    CachedDataFormat format() default CachedDataFormat.PLAIN_TEXT;
}
