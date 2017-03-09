package com.wifiin.dsl.groovy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.exception.DSLException;
import com.wifiin.util.Help;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
/**
 * 使用groovy构建的dsl。
 * @author Running
 *
 */
public class GroovyDSL{
    private GroovyDSL(){}
    public static GroovyDSL create(){
        return new GroovyDSL();
    }
    private static final Map<String,GroovyDSL> DSL_MAP=Maps.newConcurrentMap();
    private static final ThreadLocal<String> SCRIPT_TAG=new ThreadLocal<>();
    private static final String DSL_METHOD_NAME="exec";
    private Binding binding=new Binding();
    private Script script;
    private List<DSLArg> args;
    private List<String> imported;
    private List<String> staticImported;
    private String body;
    private String tag;
    private class DSLArg{
        public String type;
        public String name;
        public DSLArg(Class type,String name){
            this.type=type.getName();
            this.name=name;
        }
    }
    /**
     * 导入指定的类，集合元素可以是要导入的Class对象，也可以是要导入的类全限定类名
     * @param imported
     * @return
     * @throws DSLException 如果集合元素既不是Class也不是String，抛出此异常
     */
    public GroovyDSL addImports(Collection<?> imported){
        return addImports(imported.stream());
    }
    /**
     * 导入指定的类，参数是要导入的类
     * @param imported
     * @return
     */
    public GroovyDSL addImports(Class<?>... imported){
        return addImports(Arrays.stream(imported));
    }
    /**
     * 导入指定的类，参数是要导入的全限定类名
     * @param imported
     * @return
     */
    public GroovyDSL addImports(String... imported){
        return addImports(Arrays.stream(imported));
    }
    /**
     * 要导入的类参数是要导入的全限定类名
     * @param imported
     * @return
     * @throws DSLException 如果集合元素既不是Class也不是String，抛出此异常
     */
    public GroovyDSL addImports(Stream<?> imported){
        if(this.imported==null){
            this.imported=Lists.newArrayList();
        }
        return addImports(this.imported,imported);
    }
    /**
     * 要静态导入的类的集合，所有被导入的类都是import xxxxx.Yzzzz.*。集合元素要么是Class要么是要导入类的全限定类名
     * @param imported
     * @return
     * @throws DSLException 如果集合元素既不是Class也不是String，抛出此异常
     */
    public GroovyDSL addStaticImports(Collection<?> imported){
        return addStaticImports(imported.stream());
    }
    /**
     * 要静态导入的类，所有被导入的类都是import xxxxx.Yzzzz.*。
     * @param imported
     * @return
     */
    public GroovyDSL addStaticImports(Class<?>... imported){
        return addStaticImports(Arrays.stream(imported));
    }
    /**
     * 要静态导入的全限定类名，所有被导入的类都是import xxxxx.Yzzzz.*。
     * @param imported
     * @return
     */
    public GroovyDSL addStaticImports(String... imported){
        return addStaticImports(Arrays.stream(imported));
    }
    /**
     * 要静态导入的类的集合，所有被导入的类都是import xxxxx.Yzzzz.*。集合元素要么是Class要么是要导入类的全限定类名
     * @param imported
     * @return
     * @throws DSLException 如果集合元素既不是Class也不是String，抛出此异常
     */
    public GroovyDSL addStaticImports(Stream<?> imported){
        if(this.staticImported==null){
            this.staticImported=Lists.newArrayList();
        }
        return addImports(staticImported,imported);
    }
    /**
     * 把imported的元素遍历一遍，转化成字符串填到importedList。
     * @param importedList
     * @param imported
     * @return
     * @throws DSLException 如果集合元素既不是Class也不是String，抛出此异常
     */
    private GroovyDSL addImports(List<String> importedList,Stream<?> imported){
        imported.forEach((i)->{
            String im;
            if(i instanceof Class){
                im=((Class)i).getName();
            }else if(i instanceof String){
                im=i.toString();
            }else{
                throw new DSLException("imported params should be Class or a string as the full name of class");
            }
            importedList.add(im);
        });
        return this;
    }
    /**
     * 添加dsl变量，一个脚本一旦生成，变量值就不应再改变。为了避免引起不必要的问题，不应在dsl脚本内修改这些变量的值
     * @param name
     * @param value
     * @return
     */
    public GroovyDSL variable(String name,Object value){
        binding.setVariable(name,value);
        return this;
    }
    /**
     * 添加dsl脚本参数
     * @param type
     * @param name
     * @return
     */
    public GroovyDSL addArg(Class type,String name){
        if(args==null){
            args=Lists.newArrayList();
        }
        args.add(new DSLArg(type,name));
        return this;
    }
    /**
     * 指定dsl脚本, dsl标识用body的md5base64代替
     * @param body dsl代码
     * @return
     */
    public GroovyDSL body(String body){
        return body(MessageDigestUtil.md5Base64(body),body);
    }
    /**
     * 指定dsl脚本 
     * @param tag dsl标识
     * @param body dsl代码
     * @return
     */
    public GroovyDSL body(String tag,String body){
        this.body=body;
        this.tag=tag;
        SCRIPT_TAG.set(tag);
        return this;
    }
    /**
     * 返回dsl脚本的md5
     * @return
     */
    public String tag(){
        return tag;
    }
    /**
     * 编译dsl，这是构建dsl的最后一步
     * @return
     */
    public GroovyDSL compile(){
        GroovyShell shell=new GroovyShell(binding);
        StringBuilder script=ThreadLocalStringBuilder.builder();
        appendScriptImport(this.imported,(i)->{
            script.append("import ").append(i).append(";\n");
        });
        appendScriptImport(this.staticImported,(i)->{
            script.append("import static ").append(i).append(".*;\n");
        });
        script.append("def ").append(DSL_METHOD_NAME).append("(");
        if(Help.isNotEmpty(args)){
            for(int i=0,l=args.size();i<l;i++){
                DSLArg arg=args.get(i);
                script.append(arg.type).append(" ").append(arg.name);
                if(i<l-1){
                    script.append(",");
                }
            }
        }
        script.append("){").append(body).append(";}");
        this.script=shell.parse(script.toString());
        DSL_MAP.put(tag,this);
        return this;
    }
    /**
     * 为脚本添加导入的类
     * @param script 脚本
     * @param imported 导入的类集合
     */
    private void appendScriptImport( List<String> imported,Consumer<String> importConsumer){
        if(Help.isNotEmpty(imported)){
            for(int i=0,l=imported.size();i<l;i++){
                importConsumer.accept(imported.get(i));
            }
        }
    }
    /**
     * 编译并执行dsl
     * @param args 执行dsl的参数
     * @return dsl的返回值
     */
    public <R> R compileAndExec(Object... args){
        return compile().exec(args);
    }
    /**
     * 执行dsl
     * @param args 执行dsl的参数
     * @return dsl的返回值
     */
    @SuppressWarnings("unchecked")
    public <R> R exec(Object... args){
        return (R)this.script.invokeMethod(DSL_METHOD_NAME,args);
    }
    /**
     * 返回本线程最近一次编译的dsl
     * @return
     */
    public static GroovyDSL dsl(){
        return dsl(SCRIPT_TAG.get());
    }
    /**
     * 返回指定脚本md5值的dsl
     * @param md5
     * @return
     */
    public static GroovyDSL dsl(String tag){
        return DSL_MAP.get(tag);
    }
    /**
     * 执行本线程最近一次编译的dsl
     * @param args 执行dsl的参数，参数数量必须与定义dsl时的参数数量一致
     * @return dsl返回值
     */
    public static <R> R execCurrently(Object... args){
        return exec(SCRIPT_TAG.get(),args);
    }
    /**
     * 执行指定脚本md5值的dsl
     * @param tag 脚本的标识
     * @param args dsl的参数
     * @return dsl返回值
     */
    @SuppressWarnings("unchecked")
    public static <R> R exec(String tag,Object... args){
        return (R)dsl(tag).exec(args);
    }
    public static void main(String[] args){
        Binding binding = new Binding();  
        binding.setVariable("v",Maps.newHashMap());
        binding.setProperty("prop","propVal");//最好不要用
        GroovyShell shell = new GroovyShell(binding);  
        //直接方法调用  
        //shell.parse(new File(//))  
        String dsl="import static "+MessageDigestUtil.class.getName()+".*;\n"+"def exec(String k) {println md5Base64(k);}";
        System.out.println(dsl);
        Script script = shell.parse(dsl);
        shell=null;
        binding=null;
        script.invokeMethod("exec",new String[]{"a"});
    }
}
