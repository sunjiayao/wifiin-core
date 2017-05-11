package com.wifiin.reflect.getset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import com.wifiin.reflect.exception.NoSuchPropertyException;
import com.wifiin.reflect.exception.SetterGenerationException;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public class ClassSetterMap{
    private static <O,V> Setter<O,V> generateSetter(Class<O> clazz,Supplier<Class<V>> propertyTypeSupplier,
            Supplier<String> propertyNameSupplier,
            boolean method) throws NotFoundException,CannotCompileException,InstantiationException,IllegalAccessException{
        return (Setter<O,V>)GetSetUtil.generateSetter(clazz,propertyTypeSupplier,propertyNameSupplier,method);
    }
    
    @SuppressWarnings("unchecked")
    private static <O,V> Setter<O,V> generateSetter(Class<O> clazz,
            final Field field) throws NotFoundException,CannotCompileException,InstantiationException,IllegalAccessException{
        return (Setter<O,V>)generateSetter(clazz,()->{
            return (Class<V>)(field.getType());
        },()->{
            return field.getName();
        },false);
    }
    
    @SuppressWarnings("unchecked")
    static <O,V> Setter<O,V> getSetter(Class<O> clazz,Field field){
        if(Help.isFinalOrStaticField(field)){
            return null;
        }
        Map<String,Setter<?,?>> propertyMap=GetSetUtil.getSetterPropertyMap(clazz);
        return (Setter<O,V>)propertyMap.computeIfAbsent(field.getName(),(k)->{
            try{
                return generateSetter(clazz,field);
            }catch(InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new SetterGenerationException(clazz.getName() + '.' + field,e);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private static <O,V> Setter<O,V> generateSetter(Class<O> clazz, Method setter) 
            throws InstantiationException,IllegalAccessException,NotFoundException,CannotCompileException{
        return (Setter<O,V>)generateSetter(clazz,()->{
            return (Class<V>)(setter.getParameterTypes()[0]);
        },setter::getName,true);
    }
    
    @SuppressWarnings("unchecked")
    static <O,V> Setter<O,V> getSetter(Class<O> clazz,Method setter){
        Map<String,Setter<?,?>> propertyMap=GetSetUtil.getSetterPropertyMap(clazz);
        String propertyName=GetSetUtil.extractPropertyName(setter,false);
        return (Setter<O,V>)propertyMap.computeIfAbsent(propertyName,(k)->{
            try{
                return generateSetter(clazz,setter);
            }catch(InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new SetterGenerationException(clazz.getName() + '.' + setter,e);
            }
        });
    }
    
    private static <O,V> Setter<O,V> generateSetter(Class<O> clazz,
            String property) throws SecurityException,InstantiationException,IllegalAccessException,NotFoundException,CannotCompileException{
        try{
            Field field=clazz.getField(property);
            return generateSetter(clazz,field);
        }catch(NoSuchFieldException e){
        }
        try{
            Method setter=clazz.getMethod(property);
            return generateSetter(clazz,setter);
        }catch(NoSuchMethodException e){
        }
        String setterName=ThreadLocalStringBuilder.builder().append("set").append(property.substring(0,1).toUpperCase())
                .append(property.substring(1)).toString();
        Method setter=null;
        Method[] methods=clazz.getMethods();
        for(int i=0,l=methods.length;i < l;i++){
            Method method=methods[i];
            if(setterName.equals(method.getName())){
                setter=methods[i];
                break;
            }
        }
        if(setter == null){
            throw new NoSuchPropertyException(clazz.getName() + '.' + property);
        }
        return generateSetter(clazz,setter);
    }
    
    @SuppressWarnings("unchecked")
    static <O,V> Setter<O,V> getSetter(Class<O> clazz,String property){
        Map<String,Setter<?,?>> propertyMap=GetSetUtil.getSetterPropertyMap(clazz);
        return (Setter<O,V>)propertyMap.computeIfAbsent(property,(k)->{
            try{
                return generateSetter(clazz,property);
            }catch(SecurityException | InstantiationException | IllegalAccessException | NotFoundException | CannotCompileException e){
                throw new SetterGenerationException(clazz.getName() + '.' + property,e);
            }
        });
    }
}
