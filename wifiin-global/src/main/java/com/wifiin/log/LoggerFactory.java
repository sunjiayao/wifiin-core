package com.wifiin.log;

import java.util.Collection;
import java.util.List;

import org.slf4j.Marker;

import com.google.common.collect.Lists;
import com.wifiin.util.text.template.TextTemplateFormatterType;

public class LoggerFactory{
    public static interface Logger extends org.slf4j.Logger{
        public void debug(String message,Throwable t,Object... msgArgs);
        public void error(String message,Throwable t,Object... msgArgs);
        public void info(String message,Throwable t,Object... msgArgs);
        public void warn(String message,Throwable t,Object... msgArgs);
        public void trace(String message,Throwable t,Object... msgArgs);
    }
    public static Logger getLogger(Class cls){
        return getLogger(org.slf4j.LoggerFactory.getLogger(cls));
    }
    public static Logger getLogger(String name){
        return getLogger(org.slf4j.LoggerFactory.getLogger(name));
    }
    private static Logger getLogger(org.slf4j.Logger logger){
        return new Logger(){
            
            private String format(String message,Object o){
                if(o instanceof Collection && !(o instanceof List)){
                    List list=Lists.newArrayList();
                    list.addAll((Collection)o);
                    o=list;
                }
                return TextTemplateFormatterType.PLAIN_TEXT.formatter(message,"{","}").format(o);
            }
            
            @Override
            public void debug(String message){
                logger.debug(message);
            }

            @Override
            public void debug(String message,Object o){
                logger.debug(format(message,o));
            }

            @Override
            public void debug(String message,Object... o){
                logger.debug(format(message,o));
            }

            @Override
            public void debug(String message,Throwable e){
                logger.debug(message,e);
            }

            @Override
            public void debug(Marker marker,String message){
                logger.debug(marker,message);
            }

            @Override
            public void debug(String message,Object o1,Object o2){
                logger.debug(format(message,new Object[]{o1,o2}));
            }
            
            @Override
            public void debug(Marker marker,String o1,Object o2){
                logger.debug(marker,o1,o2);
            }

            @Override
            public void debug(Marker marker,String message,Object... o){
                logger.debug(marker,format(message,o));
            }

            @Override
            public void debug(Marker marker,String message,Throwable e){
                logger.debug(marker,message,e);
            }

            @Override
            public void debug(Marker marker,String message,Object o1,Object o2){
                logger.debug(marker,format(message,new Object[]{o1,o2}));
            }

            @Override
            public void error(String message){
                logger.error(message);
            }

            @Override
            public void error(String message,Object o){
                logger.error(format(message,o));
            }

            @Override
            public void error(String message,Object... o){
                logger.error(format(message,o));
            }

            @Override
            public void error(String message,Throwable e){
                logger.error(message,e);
            }

            @Override
            public void error(Marker marker,String message){
                logger.error(marker,message);
            }

            @Override
            public void error(String message,Object o1,Object o2){
                logger.error(format(message,new Object[]{o1,o2}));
            }

            @Override
            public void error(Marker marker,String o1,Object o2){
                logger.error(marker,o1,o2);
            }

            @Override
            public void error(Marker marker,String message,Object... o){
                logger.error(marker,format(message,o));
            }

            @Override
            public void error(Marker marker,String message,Throwable e){
                logger.error(marker,message,e);
            }

            @Override
            public void error(Marker marker,String message,Object o1,Object o2){
                logger.error(marker,format(message,new Object[]{o1,o2}));
            }
            
            @Override
            public String getName(){
                return logger.getName();
            }

            @Override
            public void info(String message){
                logger.info(message);
            }

            @Override
            public void info(String message,Object o){
                logger.info(format(message,o));
            }

            @Override
            public void info(String message,Object... o){
                logger.info(format(message,o));
            }

            @Override
            public void info(String message,Throwable e){
                logger.info(message,e);
            }

            @Override
            public void info(Marker marker,String message){
                logger.info(marker,message);
            }

            @Override
            public void info(String message,Object o1,Object o2){
                logger.info(format(message,new Object[]{o1,o2}));
            }

            @Override
            public void info(Marker marker,String o1,Object o2){
                logger.info(marker,o1,o2);
            }

            @Override
            public void info(Marker marker,String message,Object... o){
                logger.info(marker,format(message,o));
            }

            @Override
            public void info(Marker marker,String message,Throwable e){
                logger.info(marker,message,e);
            }

            @Override
            public void info(Marker marker,String message,Object o1,Object o2){
                logger.info(marker,format(message,new Object[]{o1,o2}));
            }
            
            @Override
            public boolean isDebugEnabled(){
                return logger.isDebugEnabled();
            }

            @Override
            public boolean isDebugEnabled(Marker marker){
                return logger.isDebugEnabled(marker);
            }

            @Override
            public boolean isErrorEnabled(){
                return logger.isErrorEnabled();
            }

            @Override
            public boolean isErrorEnabled(Marker marker){
                return logger.isErrorEnabled(marker);
            }

            @Override
            public boolean isInfoEnabled(){
                return logger.isInfoEnabled();
            }

            @Override
            public boolean isInfoEnabled(Marker marker){
                return logger.isInfoEnabled(marker);
            }

            @Override
            public boolean isTraceEnabled(){
                return logger.isTraceEnabled();
            }

            @Override
            public boolean isTraceEnabled(Marker marker){
                return logger.isTraceEnabled(marker);
            }

            @Override
            public boolean isWarnEnabled(){
                return logger.isWarnEnabled();
            }

            @Override
            public boolean isWarnEnabled(Marker marker){
                return logger.isWarnEnabled(marker);
            }

            @Override
            public void trace(String message){
                logger.trace(message);
            }

            @Override
            public void trace(String message,Object o){
                logger.trace(format(message,o));
            }

            @Override
            public void trace(String message,Object... o){
                logger.trace(format(message,o));
            }

            @Override
            public void trace(String message,Throwable e){
                logger.trace(message,e);
            }

            @Override
            public void trace(Marker marker,String message){
                logger.trace(marker,message);
            }

            @Override
            public void trace(String message,Object o1,Object o2){
                logger.trace(format(message,new Object[]{o1,o2}));
            }

            @Override
            public void trace(Marker marker,String o1,Object o2){
                logger.trace(marker,o1,o2);
            }

            @Override
            public void trace(Marker marker,String message,Object... o){
                logger.trace(marker,format(message,o));
            }

            @Override
            public void trace(Marker marker,String message,Throwable e){
                logger.trace(marker,message,e);
            }

            @Override
            public void trace(Marker marker,String message,Object o1,Object o2){
                logger.trace(marker,format(message,new Object[]{o1,o2}));
            }

            @Override
            public void warn(String message){
                logger.warn(message);
            }

            @Override
            public void warn(String message,Object o){
                logger.warn(format(message,o));
            }

            @Override
            public void warn(String message,Object... o){
                logger.warn(format(message,o));
            }

            @Override
            public void warn(String message,Throwable e){
                logger.warn(message,e);
            }

            @Override
            public void warn(Marker marker,String message){
                logger.warn(marker,message);
            }

            @Override
            public void warn(String message,Object o1,Object o2){
                logger.warn(format(message,new Object[]{o1,o2}));
            }

            @Override
            public void warn(Marker marker,String o1,Object o2){
                logger.warn(marker,o1,o2);
            }

            @Override
            public void warn(Marker marker,String message,Object... o){
                logger.warn(marker,format(message,o));
            }

            @Override
            public void warn(Marker marker,String message,Throwable e){
                logger.warn(marker,message,e);
            }

            @Override
            public void warn(Marker marker,String message,Object o1,Object o2){
                logger.warn(marker,format(message,new Object[]{o1,o2}));
            }

            @Override
            public void debug(String message,Throwable t,Object... msgArgs){
                debug(format(message,msgArgs),t);
            }

            @Override
            public void error(String message,Throwable t,Object... msgArgs){
                error(format(message,msgArgs),t);
            }

            @Override
            public void info(String message,Throwable t,Object... msgArgs){
                info(format(message,msgArgs),t);
            }

            @Override
            public void warn(String message,Throwable t,Object... msgArgs){
                warn(format(message,msgArgs),t);
            }

            @Override
            public void trace(String message,Throwable t,Object... msgArgs){
                trace(format(message,msgArgs),t);
            }
        };
    }
}
