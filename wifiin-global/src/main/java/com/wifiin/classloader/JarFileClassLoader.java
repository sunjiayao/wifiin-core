package com.wifiin.classloader;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.wifiin.util.string.ThreadLocalStringBuilder;

public class JarFileClassLoader extends AbstractJORClassLoader {
    static{
        registerAsParallelCapable();
    }
    
	private JarFile jar;
	
	public JarFileClassLoader(JarFile jar, ClassLoader parent, String startClassName, boolean startClassInCustomPath) {
		super(parent, startClassName, startClassInCustomPath);
		this.jar=jar;
	}

	@Override
	protected InputStream getBytecodeInputStream(String name) throws IOException, ClassNotFoundException {
		JarEntry entry=jar.getJarEntry(super.convertPackagePath(name));
		if(entry!=null){
			return jar.getInputStream(entry);
		}else{
			return super.openLibInputStream(name);
		}
	}

	@Override
	protected URL findJORResource(String name) {
		InputStream in=null;
		try {
			URL url=new URL(ThreadLocalStringBuilder.builder().append("jar:file:/").append(jar.getName().replace('\\', '/')).append("!/").append(super.convertPackagePath(name)).toString());
			in=url.openStream();
			return url;
		} catch (Exception e) {
			return null;
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
	}
	
}
