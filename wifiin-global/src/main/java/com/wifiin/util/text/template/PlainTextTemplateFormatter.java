package com.wifiin.util.text.template;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.wifiin.reflect.BeanUtil;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * 简单文本模板
 * 本类依赖ThreadLocalStringBuilder，如果使用本类的方法也使用了ThreadLocalStringBuilder请不要在那个方法内构造字符串时调用本类方法。
 * 如果本类对象的返回值是构造的一个字符串的一部分，请先调用本类，然后在使用本类的方法内使用ThreadLocalStringBuilder获取StringBuilder对象。
 * 占位符方式的文本模板，prefix和suffix之间的文本是提供数据的对象属性或map的key，
 * 占位符用“regex:”开头的表示这是一个正则表达式，包含正则表达式的模板只接收map作为数据来源，用map中找到的第一个匹配这个正则表达式的key所对应的值替换这个正则表达式占位符
 * @author Running
 *
 */
public class PlainTextTemplateFormatter<E> implements TextTemplateFormatter<E>{
    private static final String REGEX_PLACEHOLDER_PREFIX="regex:";
    private List<Value2String<E>> compiled=Lists.newArrayList();
    private String prefix;
    private String suffix;
    private String md5;
    public static enum DataType{
        INDEXED,
        PROPS;
    }
    public PlainTextTemplateFormatter(String template,String prefix,String suffix){
        this(template,prefix,suffix,MessageDigestUtil.md5Base64(template));
    }
    public PlainTextTemplateFormatter(String template,String prefix,String suffix,String md5){
        this.prefix=prefix;
        this.suffix=suffix;
        this.md5=md5;
        compile(template,prefix,suffix);
    }
    public String md5(){
        return md5;
    }
    private void compile(String template,String prefix,String suffix){
        int prefixLength=prefix.length();
        int suffixLength=suffix.length();
        int templateLength=template.length();
        int start=0;
        int end=template.indexOf(prefix);
        int tmp=template.indexOf(suffix,end+prefixLength);
        int placeholderIndex=0;
        for(;;){
            if(end>0 && tmp>0 && end<templateLength && tmp<templateLength){
                compiled.add(new OrigineValue2String<>(template.substring(start,end)));
            }else if(end<0 || tmp<0 || end>=templateLength || tmp>=templateLength){
                compiled.add(new OrigineValue2String<>(template.substring(start,templateLength)));
                break;
            }
            start=end+prefixLength;
            end=tmp;
            addPlaceholder(template,start,end,placeholderIndex++);
            start=end+suffixLength;
            end=template.indexOf(prefix,start);
            tmp=template.indexOf(suffix,end+prefixLength);
        }
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private void addPlaceholder(String template,int start,int end,int placeholderIndex){
        String placeholder=template.substring(start,end);
        if(placeholder.length()==0){
            compiled.add(new IndexedValue2String(placeholderIndex));
        }else if(placeholder.startsWith(REGEX_PLACEHOLDER_PREFIX)){
            compiled.add(new RegexKeyValue2String<E>(placeholder.substring(REGEX_PLACEHOLDER_PREFIX.length())));
        }else if(RegexUtil.isInteger(placeholder)){
            compiled.add(new IndexedValue2String(Integer.parseInt(placeholder)));
        }else{
            compiled.add(new PlainTextKeyValue2String<E>(placeholder));
        }
    }
    /**
     * 将指定参数的属性或集合元素格式化本模板。
     * @param data 如果模板中包含正则表达式，则本参数只能是map类型，map的key能匹配到的正则表达式，则key的值将填充到模板的相应位置，
     *             如果data的类型是map，则map的key必须是字符串
     *             data可以是复合数据类型、List、Map、数组等
     *             从data获取到的值必须覆盖了toString()
     *             目前不支持嵌套对象类型，也不支持以自定义对象为元素的数组和List,也不支持值是自定义对象、数组、List的map
     */
    @Override
    public String format(E data){
        StringBuilder text=ThreadLocalStringBuilder.builder();
        for(int i=0,l=compiled.size();i<l;i++){
            Value2String<E> v2s=compiled.get(i);
            text.append(v2s.format(data));
        }
        return text.toString();
    }
    private interface Value2String<T>{
        public String format(T data);
    }
    private class OrigineValue2String<T> implements Value2String<T>{
        private String text;
        public OrigineValue2String(String text){
            this.text=text;
        }
        @Override
        public String format(T data){
            return this.text;
        }
    }
    private class IndexedValue2String<T> implements Value2String<T>{
        private int index;
        private String indexText;
        private String placeholder;
        public IndexedValue2String(int index){
            this.index=index;
            this.indexText=Integer.toString(index);
            placeholder=prefix+index+suffix;
        }
        @SuppressWarnings({"rawtypes"})
        @Override
        public String format(T data){
            Object value=null;
            if(data instanceof List){
                value=((List)data).get(index);
            }else if(data.getClass().isArray()){
                value=Array.get(data,index);
            }else if(data instanceof Map){
                value=((Map)data).get(index);
                if(value==null){
                    value=((Map)data).get(indexText);
                }
            }else{
                throw new IllegalArgumentException("the parameter must be a List or an array, but it is a "+data.getClass());
            }
            if(value!=null){
                return value.toString();
            }
            return placeholder;
        }
    }
    private class PlainTextKeyValue2String<T> implements Value2String<T>{
        private String prop;
        private String placeholder;
        public PlainTextKeyValue2String(String prop){
            this.prop=prop;
            this.placeholder=prefix+prop+suffix;
        }
        @Override
        public String format(T data){
            Object value=null;
            if(data instanceof Map){
                value=((Map)data).get(prop);
            }else{
                try{
                    value=BeanUtil.get(data,prop);
                }catch(Exception e){
                    throw new IllegalArgumentException("the parameter must be a Map or a custom composite type, but it is a "+data.getClass(),e);
                }
            }
            if(value!=null){
                return value.toString();
            }
            return placeholder;
        }
    }
    private class RegexKeyValue2String<T> implements Value2String<T>{
        private Pattern regex;
        private String placeholder;
        public RegexKeyValue2String(String regex){
            this.regex=RegexUtil.getRegex(regex);
            placeholder=prefix+REGEX_PLACEHOLDER_PREFIX+regex+suffix;
        }
        @Override
        public String format(T data){
            Object value=null;
            if(data instanceof Map){
                for(Map.Entry<String,Object> entry:((Map<String,Object>)data).entrySet()){
                    if(regex.matcher(entry.getKey()).matches()){
                        value=entry.getValue();
                    }
                }
            }else{
                throw new IllegalArgumentException("the parameter must be a Map, but it is a "+data.getClass());
            }
            if(value!=null){
                return value.toString();
            }
            return placeholder;
        }
    }
}
