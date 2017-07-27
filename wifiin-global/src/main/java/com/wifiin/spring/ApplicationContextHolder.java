package com.wifiin.spring;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Sets;

public class ApplicationContextHolder implements ApplicationContextAware{
    private static ApplicationContextHolder holder;
    private Set<ApplicationContext> appctx=Sets.newConcurrentHashSet();
    public ApplicationContextHolder(){}
    
    public static ApplicationContextHolder getInstance(){
        return holder;
    }
    @Override
    public void setApplicationContext(ApplicationContext appctx) throws BeansException{
        this.appctx.add(appctx);
        if(holder!=null && holder.appctx!=null){
            this.appctx.addAll(holder.appctx);
        }
        holder=this;
    }
    public <T> T getBean(Class<T> c){
        for(ApplicationContext ctx:appctx){
            try{
                T t=ctx.getBean(c);
                if(t!=null){
                    return t;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> T getBean(String name){
        for(ApplicationContext ctx:appctx){
            try{
                T t=(T)ctx.getBean(name);
                if(t!=null){
                    return t;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> T getBean(String name,Class<T> requiredType){
        for(ApplicationContext ctx:appctx){
            try{
                T t=ctx.getBean(name,requiredType);
                if(t!=null){
                    return t;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> Map<String,T> getBeans(Class<T> c){
        for(ApplicationContext ctx:appctx){
            try{
                Map<String,T> map=ctx.getBeansOfType(c);
                if(map!=null){
                    return map;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> Map<String,T> getBeans(Class<T> c,boolean includeNonSingletons,boolean allowEagerInit){
        for(ApplicationContext ctx:appctx){
            try{
                Map<String,T> map=ctx.getBeansOfType(c,includeNonSingletons,allowEagerInit);
                if(map!=null){
                    return map;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> String[] getBeanNames(Class<T> c){
        for(ApplicationContext ctx:appctx){
            try{
                String[] names=ctx.getBeanNamesForType(c);
                if(names!=null){
                    return names;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public <T> String[] getBeanNames(Class<T> c,boolean includeNonSingletons,boolean allowEagerInit){
        for(ApplicationContext ctx:appctx){
            try{
                String[] names=ctx.getBeanNamesForType(c,includeNonSingletons,allowEagerInit);
                if(names!=null){
                    return names;
                }
            }catch(Exception e){}
        }
        return null;
    }
    
    public <T> T getBeanWithAnnotation(Class<? extends Annotation> c){
        for(ApplicationContext ctx:appctx){
            try{
                T t=(T)ctx.getBeansWithAnnotation(c);
                if(t!=null){
                    return t;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public Map<String,Object> getBeansWithAnnotation(Class<? extends Annotation> c){
        for(ApplicationContext ctx:appctx){
            try{
                Map<String,Object> map=ctx.getBeansWithAnnotation(c);
                if(map!=null){
                    return map;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public String[] getBeanNamesWithAnnotation(Class<? extends Annotation> c){
        for(ApplicationContext ctx:appctx){
            try{
                String[] names=ctx.getBeanNamesForAnnotation(c);
                if(names!=null){
                    return names;
                }
            }catch(Exception e){}
        }
        return null;
    }
    public Set<ApplicationContext> getApplicationContext(){
        return appctx;
    }
}
