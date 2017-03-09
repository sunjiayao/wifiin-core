package com.wifiin.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassPathClassLoader extends AbstractJORClassLoader {

    static{
        registerAsParallelCapable();
    }
    
	private File classpath;
	public ClassPathClassLoader(File classpath, ClassLoader parent, String startClassName, boolean startClassInCustomPath) {
		super(parent, startClassName, startClassInCustomPath);
		this.classpath=classpath;
	}

	@Override
	protected InputStream getBytecodeInputStream(String name) throws ClassNotFoundException, IOException {
		File classFile=new File(classpath,super.convertPackagePath(name));
		if(classFile.exists()){
			return new FileInputStream(classFile);
		}else{
			return super.openLibInputStream(name);
		}
	}
	
	@Override
	protected URL findJORResource(String name) {
		File res=new File(classpath,super.convertPackagePath(name));
		if(res.exists()){
			try {
				return res.toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}else{
			return null;
		}
	}
	
//	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		ClassLoader cl=new ClassPathClassLoader(new File("E:\\class"),ClassPathClassLoader.class.getClassLoader(),"test2",true);
//		System.out.println(ClassPathClassLoader.class.getClassLoader());
//		System.out.println(cl.loadClass("test2"));
//		System.out.println(cl.loadClass("test2").getClassLoader());
//		cl.loadClass("test").getMethod("main",String[].class).invoke(null,new String[1]);
//	}
}
