package com.wifiin.util.text.template;
/**
 * 文本生成模板处理器
 * @author Running
 *
 */
public interface TextTemplateFormatter<E>{
    /**
     * 按不同的实现支持Map和pojo对象，支持javaassist遍历对象get方法和public 属性
     * @param data 用模板构造文本的数据源
     * @return
     */
    public String format(E data);
    /**
     * 模板的md5base64
     * @return
     */
    public String md5();
}
