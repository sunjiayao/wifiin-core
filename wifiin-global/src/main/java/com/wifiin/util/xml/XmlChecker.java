package com.wifiin.util.xml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public class XmlChecker extends BatchXSLTExecutor{
	
	private OutputStream reportOut=System.out;
	
	public XmlChecker() {}
	public XmlChecker(String ruleRegex,String charset) throws TransformerConfigurationException, IOException{
		super(XmlChecker.class.getResource("/").getFile().toString(),"rule-esdef",charset);
	}
	public XmlChecker(String ruleRegex, String output,String charset) throws TransformerConfigurationException, IOException{
		this(ruleRegex,new File(output),charset);
		
	}
	public XmlChecker(String ruleRegex, File output,String charset) throws TransformerConfigurationException, IOException{
		this(ruleRegex,charset);
		if(!output.exists()){
			output.createNewFile();
		}
		this.reportOut=new FileOutputStream(output);
	}
	public XmlChecker(String ruleRegex, OutputStream output,String charset) throws TransformerConfigurationException, IOException{
		this(ruleRegex,charset);
		this.reportOut=output;
	}
	
	public void check(String src, boolean recursive,String charset) throws TransformerException, IOException{
		check(new File(src), recursive,charset);
	}
	public void check(File src, final boolean recursive,String charset) throws TransformerException, IOException{
		if(src.isFile() && src.getName().endsWith(".xml")){
			super.transform(src, reportOut,charset);
		}else if(src.isDirectory()){
			for(File f:src.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname) {
					return (pathname.isFile() && pathname.getName().endsWith(".xml")) || (recursive && pathname.isDirectory());
				}})){
				check(f,recursive,charset);
			}
		}
	}
	public void close() throws IOException{
		reportOut.close();
	}
}