package com.wifiin.util.text.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;
import com.wifiin.util.text.template.exception.TextTemplateFormatterException;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
/**
 * 接收一切符合groovy.lang.GString内嵌表达式语法的字符串作为模板
 * @author Running
 *
 * @param <T>
 */
public class GroovyTextTemplateFormatter<T> implements TextTemplateFormatter<T>{
    private Script script;
    private String md5;
    public GroovyTextTemplateFormatter(String template){
        this(template,MessageDigestUtil.md5Base64(template));
    }
    public GroovyTextTemplateFormatter(String template,String md5){
        this.md5=md5;
        try(GroovyClassLoader groovyLoader = new GroovyClassLoader();){
            StringBuilder scriptBuilder=ThreadLocalStringBuilder.builder();
            String scriptSrc=scriptBuilder.append("def eval(Object data) {\"").append(template).append("\";}").toString();
            Class<Script> groovyClass=(Class<Script>)groovyLoader.parseClass(scriptSrc);
            script=groovyClass.newInstance();
        }catch(Exception e){
            throw new TextTemplateFormatterException(e);
        }
    }
    public String md5(){
        return md5;
    }
    @Override
    public String format(Object data){
        return script.invokeMethod("eval",data).toString();
    }
    public static void main(String[] args) throws InstantiationException, IllegalAccessException{
        Binding binding = new Binding();  
        binding.setVariable("v",Maps.newHashMap());
        binding.setProperty("prop","propVal");//最好不要用
        GroovyShell shell = new GroovyShell(binding);  
        //直接方法调用  
        //shell.parse(new File(//))  
        Script script = shell.parse("def exec(String k) {\"${k}:${v}|${prop}\";}");
        shell=null;
        binding=null;
        script.invokeMethod("exec",Lists.newArrayList("a"));//如果第二个参数是数组或List，而参数不是，它会被展开再传递到调用的方法，以下两种方式也能调用成功
        script.invokeMethod("exec",new String[]{"a"});
        System.out.println(script.invokeMethod("exec","a"));
    }
}
