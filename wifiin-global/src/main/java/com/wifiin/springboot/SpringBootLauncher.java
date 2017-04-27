package com.wifiin.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
/**
 * 如果其它工程需要自定义启动过程，可以调用SpringBootLauncher.launch(new String[]{....},"**\/applicationContext**.xml",Xxxx.class);
 * Xxxx是任意类的名字，它的类名定义前面只需要包含@ComponentScan(value=".....")注解
 * 然后在这行调用的代码前后增加自定义处理逻辑
 */
@Configuration 
@ComponentScan(value="com.wifiin.**")
@EnableAutoConfiguration
@SpringBootApplication
public class SpringBootLauncher {
    private static final Logger log=LoggerFactory.getLogger(SpringBootLauncher.class);
    
    public static ConfigurableApplicationContext launch(String appName,String[] args, Object... conf){
        long start=System.currentTimeMillis();
        int confLen=conf==null?0:conf.length;
        Object[] confs=new Object[confLen+1];
        if(conf!=null){
            System.arraycopy(conf, 0, confs, 1, confLen);
        }
        confs[0]=SpringBootLauncher.class;
        ConfigurableApplicationContext context=SpringApplication.run(confs,args);
        log.info("{} started in: {} ms",appName,(System.currentTimeMillis()-start));
        return context;
    }
    
    public static void main(String[] args){
        launch("",args,"**/applicationContext*.xml");
    }
}
