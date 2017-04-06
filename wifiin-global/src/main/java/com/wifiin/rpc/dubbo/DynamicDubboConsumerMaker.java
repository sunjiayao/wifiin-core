package com.wifiin.rpc.dubbo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.wifiin.common.CommonConstant;
import com.wifiin.reflect.ClassForNameMap;
import com.wifiin.rpc.dubbo.exception.DynamicDubboConsumerMakerException;
import com.wifiin.util.io.IOUtil;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import javassist.ClassPool;
import javassist.CtClass;

@Deprecated
public class DynamicDubboConsumerMaker{
    private static final Map<String,DynamicDubboConsumerWrapper> DYNAMIC_DUBBO_CONSUMER_MAP=Maps.newConcurrentMap();
    private static final Pattern DOT_REGEX=RegexUtil.getRegex("\\.");
    private static final String DUBBO_CALLBACK_CONSUMER_TEMPLATE="/dubbo-callback-consumer-template.xml";
    private static final String USER_CENTER_CALLBACK_CONSUMER_ID="#UserCenterCallbackConsumerId#";
    private static final String USER_CENTER_CALLBACK_INTERFACE="#UserCenterCallbackInterface#";
    private static final String USER_CENTER_CALLBACK_CONSUMERS_CONF_PATH="/com/wifiin/platform/user/callback/consumers/conf/";
    private static final String USER_CENTER_DUBBO_CONSUMER_CALLBAK_PREFIX=USER_CENTER_CALLBACK_CONSUMERS_CONF_PATH+"dubbo-consumer-callback-";
    private static final String USER_CENTER_DUBBO_CONSUMER_CALLBACK_CLASSPATH="classpath:"+USER_CENTER_CALLBACK_CONSUMERS_CONF_PATH;
    private static final String USER_CENTER_DUBBO_CONSUMER_CALLBACK_TEMPLATE;
    static{
        try(InputStream in=DynamicDubboConsumerMaker.class.getResourceAsStream(DUBBO_CALLBACK_CONSUMER_TEMPLATE)){
            USER_CENTER_DUBBO_CONSUMER_CALLBACK_TEMPLATE=IOUtil.readString(in,CommonConstant.DEFAULT_CHARSET_NAME);
        }catch(IOException e){
            throw new DynamicDubboConsumerMakerException(e);
        }
    }
    private static final class DynamicDubboConsumerWrapper{
        private Object consumer;
        private ApplicationContext context;
        public DynamicDubboConsumerWrapper(Object consumer,ApplicationContext context){
            this.consumer=consumer;
            this.context=context;
        }
        @SuppressWarnings("unchecked")
        public <E> E consumer(){
            return (E)consumer;
        }
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <E> E consumer(String interfaceClassName,Class<E> superInterfaceClass){
        if(superInterfaceClass.getName().equals(interfaceClassName)){
            throw new IllegalArgumentException("interfaceClassName must not be same as the name of interfaceClass");
        }
        return (E)DYNAMIC_DUBBO_CONSUMER_MAP.computeIfAbsent(interfaceClassName,(n)->{
            try{
                ClassPool cp=ClassPool.getDefault();
                Class c=null;
                CtClass cc=null;
                try{
                    c=ClassForNameMap.get(interfaceClassName);
                    cc=cp.get(interfaceClassName);
                }catch(Throwable e){
                    cc=cp.makeInterface(interfaceClassName,cp.get(superInterfaceClass.getName()));
                    c=cc.toClass();
                }
                
                String callbackConsumerTag=DOT_REGEX.matcher(c.getName()).replaceAll("-");
                StringBuilder consumerFileNameBuilder=ThreadLocalStringBuilder.builder();
                consumerFileNameBuilder.append(USER_CENTER_DUBBO_CONSUMER_CALLBAK_PREFIX).append(callbackConsumerTag).append(".xml");
                String consumerConfFileName=consumerFileNameBuilder.toString();
                File consumerFile=new File(new File(superInterfaceClass.getResource("/").toURI()),consumerConfFileName);
                if(consumerFile.exists()){
                    consumerFile.delete();
                }
                if(!consumerFile.getParentFile().exists()){
                    consumerFile.getParentFile().mkdirs();
                }
                consumerFile.createNewFile();
                
                StringBuilder xml=ThreadLocalStringBuilder.builder();
                xml.append(USER_CENTER_DUBBO_CONSUMER_CALLBACK_TEMPLATE);
                int idx=xml.indexOf(USER_CENTER_CALLBACK_CONSUMER_ID);
                xml.replace(idx,idx+USER_CENTER_CALLBACK_CONSUMER_ID.length(),callbackConsumerTag);
                idx=xml.indexOf(USER_CENTER_CALLBACK_INTERFACE);
                xml.replace(idx,idx+USER_CENTER_CALLBACK_INTERFACE.length(),c.getName());
                try(PrintStream ps=new PrintStream(consumerFile)){
                    ps.print(xml);
                }
                StringBuilder consumerClasspath=ThreadLocalStringBuilder.builder();
                consumerClasspath.append(USER_CENTER_DUBBO_CONSUMER_CALLBACK_CLASSPATH).append(consumerFile.getName());
                ApplicationContext callbackContext=new ClassPathXmlApplicationContext(consumerClasspath.toString());
                
                return new DynamicDubboConsumerWrapper(callbackContext.getBean(callbackConsumerTag,c),callbackContext);
            }catch(Exception e){
                throw new DynamicDubboConsumerMakerException(e);
            }
        }).consumer();
    }
}
