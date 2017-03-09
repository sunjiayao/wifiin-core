package com.wifiin.redis.aop.mybatis;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import com.wifiin.jdbc.Page;
import com.wifiin.util.Help;

public class MybatisPageUtil {
	private static MethodHandles.Lookup lookup=MethodHandles.lookup();
	
	public static <T> Page<T> findPage(Object dao, String countMethod,String listMethod,Class<T> modelClass,int page,int pageSize,Object... params) throws NoSuchMethodException, IllegalAccessException, Throwable{
		return findPage(dao,countMethod,listMethod,modelClass,page,pageSize,(Object)params);
	}
	public static <T> Page<T> findPage(Object dao,String countMethod,String listMethod,Class<T> modelClass,int page,int pageSize,List params) throws NoSuchMethodException, IllegalAccessException, Throwable{
		return findPage(dao,countMethod,listMethod,modelClass,page,pageSize,(Object)params);
	}
	public static <T> Page<T> findPage(Object dao,String countMethod,String listMethod,Class<T> modelClass,int page, int pageSize,Map<String,?> params) throws NoSuchMethodException, IllegalAccessException, Throwable{
		return findPage(dao,countMethod,listMethod,modelClass,page,pageSize,(Object)params);
	}
	public static <T> Page<T> findPage(Object dao,String countMethod,String listMethod,Class<T> modelClass,int page,int pageSize,Object params) throws NoSuchMethodException, IllegalAccessException, Throwable{
		Class[] types=null;
		Object[] countArgs=null;
		Object[] listArgs=null;
		if(Help.isNotEmpty(params)){
			if(params.getClass().isArray() || (Object)params instanceof List){
				int len=Array.getLength(params);
				countArgs=new Object[1+len];
				listArgs=new Object[3+len];
				types=new Class[len];
				for(int i=0,l=len;i<l;i++){
					Object el=Array.get(params, i);
					types[i]=el.getClass();
					countArgs[i+1]=el;
					listArgs[i+3]=el;
				}
			}else{
				types=new Class[]{params.getClass()};
				countArgs=new Object[2];
				listArgs=new Object[4];
				countArgs[1]=listArgs[3]=params;
			}
		}else{
			countArgs=new Object[1];
			listArgs=new Object[3];
		}
		countArgs[0]=listArgs[0]=dao;
		listArgs[2]=pageSize;
		return findPage(dao,countArgs,listArgs,page,pageSize,modelClass,params);
	}
	private static <T> Page<T> findPage(Object dao,Object[] countArgs,Object[] listArgs,int page,int pageSize,Class<T> modelClass,Object params) throws NoSuchMethodException, IllegalAccessException, Throwable{
		int count=(Integer)lookup.findVirtual(dao.getClass(),"countMethod",
				(MethodType)lookup.findVirtual(MethodType.class, "methodType", 
						MethodType.methodType(int.class, params.getClass())).invokeWithArguments()).invokeWithArguments(countArgs);
		if(count>0){
			if(pageSize<=0){
				pageSize=count;
			}
			int totalPage = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
			listArgs[1]=(page-1)*pageSize;
			return new Page<T>((List)lookup.findVirtual(dao.getClass(),"listMethod",
					(MethodType)lookup.findVirtual(MethodType.class, "methodType", 
							MethodType.methodType(modelClass, params.getClass())).invokeWithArguments())
							.invokeWithArguments(listArgs), page, count, totalPage);
		}else{
			return new Page<T>();
		}
	}
}
