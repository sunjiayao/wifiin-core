package com.wifiin.reflect.getset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.wifiin.util.string.ThreadLocalStringBuilder;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute;

public class GetSetUtil{
    private static final ClassPool CLASS_POOL=ClassPool.getDefault();
    private static final Map<Method,String> METHOD_PROPERTY_NAME_MAP=Maps.newConcurrentMap();
    private static final String GETTER_PACKAGE_NAME=(ClassGetterMap.class.getPackage().getName()+".getters").intern();
    private static final String SETTER_PACKAGE_NAME=(ClassSetterMap.class.getPackage().getName()+".setters").intern();
    private static final Map<Class<?>,Map<String,Getter<?,?>>> CLASS_GETTER_MAP=Maps.newConcurrentMap(); 
    private static final Map<Class<?>,Map<String,Setter<?,?>>> CLASS_SETTER_MAP=Maps.newConcurrentMap();
    private static final Map<Class,Class> PRIMITIVE_WRAPPER_MAP=ImmutableMap.<Class,Class>builder()
            .put(int.class,Integer.class)
            .put(long.class,Long.class)
            .put(byte.class,Byte.class)
            .put(short.class,Short.class)
            .put(char.class,Character.class)
            .put(boolean.class,Boolean.class)
            .put(float.class,Float.class)
            .put(double.class,Double.class)
            .build();
    private static final Map<Class,String> WRAPPED_VALUE_METHOD_MAP=ImmutableMap.<Class,String>builder()
            .put(Integer.class,".intValue")
            .put(Long.class,".longValue")
            .put(Byte.class,".byteValue")
            .put(Short.class,".shortValue")
            .put(Character.class,".charValue")
            .put(Boolean.class,".booleanValue")
            .put(Float.class,".floatValue")
            .put(Double.class,".doubleValue")
            .build();
    private static Class getClass(Class clazz){
        Class c=PRIMITIVE_WRAPPER_MAP.get(clazz);
        return c==null?clazz:c;
    }
    @SuppressWarnings("unchecked")
    static <O,V> Getter<O,V> generateGetter(Class<O> clazz,Supplier<Class<V>> propertyTypeSupplier,Supplier<String> propertyNameSupplier,boolean method) throws InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException{
        return (Getter<O,V>)generateXetter(clazz,propertyTypeSupplier,propertyNameSupplier,method,true);
    }
    @SuppressWarnings("unchecked")
    static <O,V> Setter<O,V> generateSetter(Class<O> clazz,Supplier<Class<V>> propertyTypeSupplier,Supplier<String> propertyNameSupplier,boolean method) throws InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException{
        return (Setter<O,V>)generateXetter(clazz,propertyTypeSupplier,propertyNameSupplier,method,false);
    }
    private static <O,V> Object generateXetter(Class<O> clazz,Supplier<Class<V>> propertyTypeSupplier,Supplier<String> propertyNameSupplier,boolean method,boolean getter) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException{
        Class<V> propertyType=propertyTypeSupplier.get();
        String propertyTypeName=propertyType.getName();
        Class<V> propertyWrappedType=getClass(propertyType);
        String propertyName=propertyNameSupplier.get();
        String clazzName=clazz.getName();
        String className=
                ThreadLocalStringBuilder.builder()
                    .append(clazzName.substring(clazzName.lastIndexOf('.')+1))
                    .append("_")
                    .append(propertyName)
                    .toString();
        String classFullName=
                ThreadLocalStringBuilder.builder()
                    .append(getter?GETTER_PACKAGE_NAME:SETTER_PACKAGE_NAME)
                    .append('.')
                    .append(clazzName)
                    .append("_")
                    .append(propertyName)
                    .toString();
        CtClass xetterClass=CLASS_POOL.makeClass(classFullName);
        String superInterfaceName=getter?Getter.class.getName():Setter.class.getName();
        CtClass superInterface=CLASS_POOL.get(superInterfaceName);
        String genericXetterName=ThreadLocalStringBuilder.builder().append(superInterfaceName).append("<").append(clazz.getName()).append(",").append(propertyWrappedType.getName()).append(">").toString();
        xetterClass.setGenericSignature(new SignatureAttribute.TypeVariable(genericXetterName).encode());
        xetterClass.addInterface(superInterface);
        String xetBody=generateXetBody(clazz,propertyWrappedType,propertyName,method,getter);
        xetterClass.addMethod(CtNewMethod.make(xetBody, xetterClass));
        if(!getter){
            String propertyTypeBody=ThreadLocalStringBuilder.builder()
                .append("public Class propertyType(){")
                    .append("return ").append(propertyTypeName).append(".class;")
                .append("}").toString();
            xetterClass.addMethod(CtNewMethod.make(propertyTypeBody,xetterClass));
        }
        String constructorBody=ThreadLocalStringBuilder.builder()
            .append("public ").append(className).append("(){}").toString();
        xetterClass.addConstructor(CtNewConstructor.make(constructorBody,xetterClass));
        return xetterClass.toClass(clazz.getClassLoader(),clazz.getProtectionDomain()).newInstance();
    }
    private static <O,V> String generateXetBody(Class<O> clazz,Class<V> propertyWrappedType,String propertyName,boolean method,boolean getter){
        return getter?generateGetBody(clazz,propertyWrappedType,propertyName,method):generateSetBody(clazz,propertyWrappedType,propertyName,method);
    }
    private static <O,V> String generateGetBody(Class<O> clazz,Class<V> propertyWrappedType,String propertyName,boolean method){
        StringBuilder getterBuilder=ThreadLocalStringBuilder.builder();
        getterBuilder.append("public Object get(Object o){");
        if(WRAPPED_VALUE_METHOD_MAP.containsKey(propertyWrappedType)){
            getterBuilder.append("return ").append(propertyWrappedType.getName()).append(".valueOf(((").append(clazz.getName()).append(")o).").append(propertyName).append(method?"());":");");
        }else{
            getterBuilder.append("return ((").append(clazz.getName()).append(")o).").append(propertyName).append(method?"();":";");
        }
        getterBuilder.append("}").toString();
        return getterBuilder.toString();
    }
    private static <O,V> String generateSetBody(Class<O> clazz,Class<V> propertyWrappedType,String propertyName,boolean method){
        StringBuilder setterBuilder=ThreadLocalStringBuilder.builder();
        setterBuilder.append("public void set(Object o, Object v){");
        setterBuilder.append("((").append(clazz.getName()).append(")o).").append(propertyName);
        String unwrapMethodName=WRAPPED_VALUE_METHOD_MAP.get(propertyWrappedType);
        if(unwrapMethodName!=null){
            if(method){
                setterBuilder.append("(((").append(propertyWrappedType.getName()).append(")v)").append(unwrapMethodName).append("());}");
            }else{
                setterBuilder.append("=((").append(propertyWrappedType.getName()).append(")v)").append(unwrapMethodName).append("();}");
            }
        }else{
            if(method){
                setterBuilder.append("((").append(propertyWrappedType.getName()).append(")v);}");
            }else{
                setterBuilder.append("=(").append(propertyWrappedType.getName()).append(")v;}");
            }
        }
        setterBuilder.append("}");
        return setterBuilder.toString();
    }
    static <O> Map<String,Getter<?,?>> getGetterPropertyMap(Class<O> clazz){
        return CLASS_GETTER_MAP.computeIfAbsent(clazz,(k)->{
            return Maps.newConcurrentMap();
        });
    }
    static <O> Map<String,Setter<?,?>> getSetterPropertyMap(Class<O> clazz){
        return CLASS_SETTER_MAP.computeIfAbsent(clazz,(k)->{
            return Maps.newConcurrentMap();
        });
    }
    static String extractPropertyName(Method xetter,boolean getter){
        return METHOD_PROPERTY_NAME_MAP.computeIfAbsent(xetter,(g)->{
            String pname=xetter.getName();
            if((getter && pname.startsWith("get") && !pname.equals("get")) || (!getter && pname.startsWith("set") && !pname.equals("set"))){
                pname=ThreadLocalStringBuilder.builder().append(pname.substring(3,4).toLowerCase()).append(pname.substring(4)).toString();
            }else if(getter && pname.startsWith("is") && !pname.equals("is")){
                pname=ThreadLocalStringBuilder.builder().append(pname.substring(2,3).toLowerCase()).append(pname.substring(3)).toString();
            }
            return pname;
        });
    }
    public static <O> Map<String, Getter<?, ?>> getGetters(Class<O> clazz){
        Map<String, Getter<?, ?>> getters=CLASS_GETTER_MAP.get(clazz);
        if(getters==null){
            synchronized(clazz){
                getters=CLASS_GETTER_MAP.get(clazz);
                if(getters==null){
                    Field[] fields=clazz.getFields();
                    if(fields!=null){
                        for(int i=0,l=fields.length;i<l;i++){
                            Field field=fields[i];
                            ClassGetterMap.getGetter(clazz,field);
                        }
                    }
                    Method[] methods=clazz.getMethods();
                    if(methods!=null){
                        for(int i=0,l=methods.length;i<l;i++){
                            Method method=methods[i];
                            if(method.getName().startsWith("get")){
                                ClassGetterMap.getGetter(clazz,method);
                            }
                        }
                    }
                    getters=Collections.unmodifiableMap(getGetterPropertyMap(clazz));
                    ThreadLocalStringBuilder.builder();
                    CLASS_GETTER_MAP.put(clazz,getters);
                }
            }
        }
        return getters;
    }
    public static <O> Map<String, Setter<?, ?>> getSetters(Class<O> clazz){
        Map<String,Setter<?,?>> setters=CLASS_SETTER_MAP.get(clazz);
        if(setters==null){
            synchronized(clazz){
                setters=CLASS_SETTER_MAP.get(clazz);
                if(setters==null){
                    Field[] fields=clazz.getFields();
                    if(fields!=null){
                        for(int i=0,l=fields.length;i<l;i++){
                            Field field=fields[i];
                            ClassSetterMap.getSetter(clazz,field);
                        }
                    }
                    Method[] methods=clazz.getMethods();
                    if(methods!=null){
                        for(int i=0,l=methods.length;i<l;i++){
                            Method method=methods[i];
                            if(method.getName().startsWith("set")){
                                ClassSetterMap.getSetter(clazz,method);
                            }
                        }
                    }
                    setters=Collections.unmodifiableMap(getSetterPropertyMap(clazz));
                    ThreadLocalStringBuilder.builder();
                    CLASS_SETTER_MAP.put(clazz,setters);
                }
            }
        }
        return setters;
    }
}
