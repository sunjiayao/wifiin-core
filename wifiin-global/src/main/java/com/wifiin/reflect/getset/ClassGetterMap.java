package com.wifiin.reflect.getset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.wifiin.reflect.exception.GetterGenerationException;
import com.wifiin.reflect.exception.NoSuchPropertyException;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public class ClassGetterMap{
    private static <O,V> Getter<O,V> generateGetter(Class<O> clazz,Supplier<Class<V>> propertyTypeSupplier,Supplier<String> propertyNameSupplier,boolean method) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException{
        return GetSetUtil.generateGetter(clazz,propertyTypeSupplier,propertyNameSupplier,method);
    }
    @SuppressWarnings("unchecked")
    private static <O,V> Getter<O,V> generateGetter(Class<O> clazz,Field field) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException{
        return (Getter<O,V>)generateGetter(clazz,()->{return field.getType();},()->{return field.getName();},false);
    }
    @SuppressWarnings("unchecked")
    static <O,V> Getter<O,V> getGetter(Class<O> clazz,Field field){
        Map<String,Getter<?,?>> propertyMap=GetSetUtil.getGetterPropertyMap(clazz);
        return (Getter<O,V>)propertyMap.computeIfAbsent(field.getName(),(k)->{
            try{
                return generateGetter(clazz,field);
            }catch(InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new GetterGenerationException(clazz.getName()+'.'+field,e);
            }
        });
    }
    @SuppressWarnings("unchecked")
    private static <O,V> Getter<O,V> generateGetter(Class<O> clazz,Method getter) throws InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException{
        return (Getter<O,V>)generateGetter(clazz,()->{return getter.getReturnType();},()->{return getter.getName();},true);
    }
    @SuppressWarnings("unchecked")
    static <O,V> Getter<O,V> getGetter(Class<O> clazz,Method getter){
        Map<String,Getter<?,?>> propertyMap=GetSetUtil.getGetterPropertyMap(clazz);
        String propertyName=GetSetUtil.extractPropertyName(getter,true);
        return (Getter<O,V>)propertyMap.computeIfAbsent(propertyName,(k)->{
            try{
                return generateGetter(clazz,getter);
            }catch(InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new GetterGenerationException(clazz.getName()+'.'+getter,e);
            }
        });
    }
    private static <O,V> Getter<O,V> generateGetter(Class<O> clazz,String property) throws SecurityException, InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException{
        try{
            Field field=clazz.getField(property);
            return generateGetter(clazz,field);
        }catch(NoSuchFieldException e){}
        try{
            Method getter=clazz.getMethod(property);
            return generateGetter(clazz,getter);
        }catch(NoSuchMethodException e){}
        try{
            Method getter=clazz.getMethod(ThreadLocalStringBuilder.builder().append("get").append(property.substring(0,1).toUpperCase()).append(property.substring(1)).toString());
            return generateGetter(clazz,getter);
        }catch(NoSuchMethodException e){}
        try{
            Method getter=clazz.getMethod(ThreadLocalStringBuilder.builder().append("is").append(property.substring(0,1).toUpperCase()).append(property.substring(1)).toString());
            return generateGetter(clazz,getter);
        }catch(NoSuchMethodException e){}
        throw new NoSuchPropertyException(clazz.getName()+'.'+property);
    }
    @SuppressWarnings("unchecked")
    static <O,V> Getter<O,V> getGetter(Class<O> clazz, String property){
        Map<String,Getter<?,?>> propertyMap=GetSetUtil.getGetterPropertyMap(clazz);
        return (Getter<O,V>)propertyMap.computeIfAbsent(property,(k)->{
            try{
                return generateGetter(clazz,property);
            }catch(SecurityException | InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new GetterGenerationException(clazz.getName()+'.'+property,e);
            }
        });
    }
    
}
