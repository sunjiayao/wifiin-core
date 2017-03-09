package com.wifiin.util.text.template;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wifiin.util.digest.MessageDigestUtil;
/**
 * 文本模板工厂，如果没有指定tag就使用template的md5base64作为tag
 * @author Running
 *
 */
public class TextTemplateFormatterFactory{
    @SuppressWarnings("rawtypes")
    private static final Map<String,TextTemplateFormatter> TEMPLATE_MAP=Maps.newConcurrentMap();
    public static <T> TextTemplateFormatter<T> getPlainTextTemplateFormatter(String template,String prefix,String suffix){
        return getPlainTextTemplateFormatter(template,template,prefix,suffix);
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getPlainTextTemplateFormatter(String tag,String template,String prefix,String suffix){
        return TEMPLATE_MAP.computeIfAbsent(tag,(t)->{
            return new PlainTextTemplateFormatter<T>(template,prefix,suffix,t);
        });
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getPlainTextTemplateFormatterByTAG(String tag){
        return TEMPLATE_MAP.get(tag);
    }
    public static <T> TextTemplateFormatter<T> getFreeMarkerTextTemplateFormatter(String template){
        return getFreeMarkerTextTemplateFormatter(template,template);
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getFreeMarkerTextTemplateFormatter(String tag,String template){
        return TEMPLATE_MAP.computeIfAbsent(tag,(t)->{
            return new FreeMarkerTextTemplateFormatter<T>(template,t);
        });
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getFreeMarkerTextTemplateFormatterByTAG(String tag){
        return TEMPLATE_MAP.get(tag);
    }
    public static <T> TextTemplateFormatter<T> getGroovyTextTemplateFormatter(String template){
        return getGroovyTextTemplateFormatter(template,template);
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getGroovyTextTemplateFormatter(String tag,String template){
        return TEMPLATE_MAP.computeIfAbsent(tag,(t)->{
            return new GroovyTextTemplateFormatter<T>(template,t);
        });
    }
    @SuppressWarnings("unchecked")
    public static <T> TextTemplateFormatter<T> getGroovyTextTemplateFormatterByTAG(String tag){
        return TEMPLATE_MAP.get(tag);
    }
}
