package com.wifiin.util.text.template;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import com.wifiin.util.bytes.ThreadLocalByteArray;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.io.ThreadLocalByteArrayOutputStream;
import com.wifiin.util.text.template.exception.TextTemplateFormatterException;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 接收一切符合freemarker模板语法的字符串作为模板源
 * @author Running
 *
 * @param <T>
 */
public class FreeMarkerTextTemplateFormatter<T> implements TextTemplateFormatter<T>{
    private static final Configuration CONF=new Configuration(Configuration.getVersion());
    private static final StringTemplateLoader STRING_TEMPLATE_LOADER=new StringTemplateLoader();
    static{
        CONF.setTemplateLoader(STRING_TEMPLATE_LOADER);
    }
    private String templateMD5;
    public FreeMarkerTextTemplateFormatter(String template){
        this(template,MessageDigestUtil.md5Base64(template));
    }
    public FreeMarkerTextTemplateFormatter(String template,String templateMD5){
        this.templateMD5=templateMD5;
        if(STRING_TEMPLATE_LOADER.findTemplateSource(templateMD5)==null){
            STRING_TEMPLATE_LOADER.putTemplate(templateMD5,template);
        }
    }
    public String md5(){
        return templateMD5;
    }
    @Override
    public String format(T data){
        try(ByteArrayOutputStream out=ThreadLocalByteArrayOutputStream.stream();
            PrintWriter writer=new PrintWriter(out);){
            Template template=CONF.getTemplate(templateMD5);
            template.process(data,writer);
            writer.flush();
            return ThreadLocalByteArrayOutputStream.toText(out);
        }catch(Exception e){
            throw new TextTemplateFormatterException(e);
        }
    }
}
