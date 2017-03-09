package com.wifiin.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;

public class BatchXSLTExecutor {
    private static final Logger log=LoggerFactory.getLogger(BatchXSLTExecutor.class);
    public static final String XSLT_EXECUTOR_CHARSET="xslt.executor.charset";
	private static String DEFAULT_METHOD = "html";
	private static String DEFAULT_VERSION = "4.0";
	private static final ConcurrentHashMap<String, Transformer> transformerMap = new ConcurrentHashMap<String, Transformer>();
	private List<Transformer> transformerList=new ArrayList<Transformer>();
	
	static{
		try {
			String path=BatchXSLTExecutor.class.getResource("/").getFile().toString();
			init(path,"^rule-.*$",Help.convert(System.getProperty(XSLT_EXECUTOR_CHARSET), CommonConstant.DEFAULT_CHARSET_NAME));
		} catch (Exception e) {
			log.error("error occured in static block of class "+BatchXSLTExecutor.class.getName(), e);
		}
	}
	
	private static TransformerFactory newTransformerFactory() {
		return TransformerFactory.newInstance();
	}
	private static void init(Map<String, String> xsltmap, String destCharset,String method, String version,String charset)
			throws TransformerConfigurationException, IOException {
		TransformerFactory factory = newTransformerFactory();
		if (Help.isEmpty(method)) {
			method = DEFAULT_METHOD;
		}
		if (Help.isEmpty(version)) {
			version = DEFAULT_VERSION;
		}
		if(Help.isEmpty(destCharset)){
			destCharset=CommonConstant.DEFAULT_CHARSET_NAME;
		}
		for (Map.Entry<String, String> entry : xsltmap.entrySet()) {
			File path = new File(entry.getKey());
			final String xsltRegex = entry.getValue();
			for (File xslt : path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xslt") && name.matches(xsltRegex)) {
						return true;
					} else {
						return false;
					}
				}
			})) {
				String xsltpath = xslt.toString();
				if (!transformerMap.containsKey(xsltpath)) {
					synchronized (transformerMap) {
						if (!transformerMap.containsKey(xsltpath)) {
							Source xsltSource = new StreamSource(new InputStreamReader(new FileInputStream(xslt),charset));
							Transformer transformer = factory.newTransformer(xsltSource);
							transformer.setOutputProperty(OutputKeys.ENCODING,destCharset);
							transformer.setOutputProperty(OutputKeys.METHOD,method);
							transformer.setOutputProperty(OutputKeys.VERSION,version);
							transformerMap.put(xsltpath, transformer);
						}
					}
				}
			}
		}
	}
	private static void init(String xsltPath, String xsltNameRegex,String charset) throws TransformerConfigurationException, IOException{
		init(xsltPath, xsltNameRegex,null,null,null,charset);
	}
	private static void init(String xsltPath, String xsltNameRegex, String destCharset,String method, String version,String charset) throws TransformerConfigurationException, IOException{
		Map<String,String> map=new HashMap<String,String>();
		map.put(xsltPath, xsltNameRegex);
		init(map,destCharset,method,version,charset);
	}
	
	public BatchXSLTExecutor(){
		for(Transformer transformer:transformerMap.values()){
			transformerList.add(transformer);
		}
	}
	public BatchXSLTExecutor(String xsltPath, final String xsltNameRegex,String charset) throws TransformerConfigurationException, IOException{
		this(xsltPath, xsltNameRegex,null,null,null,charset);
	}
	public BatchXSLTExecutor(String xsltPath, final String xsltNameRegex, String destCharset,String method, String version,String charset) throws TransformerConfigurationException, IOException{
		init(xsltPath,xsltNameRegex, destCharset,method,version,charset);
		File path=new File(xsltPath);
		for (File xslt : path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xslt") && name.matches(xsltNameRegex)) {
					return true;
				} else {
					return false;
				}
			}
		})){
			transformerList.add(transformerMap.get(xslt.toString()));
		}
	}
	public BatchXSLTExecutor(Map<String, String> xsltRegexMap,String charset) throws TransformerConfigurationException, IOException{
		this(xsltRegexMap,null,null,null,charset);
	}
	public BatchXSLTExecutor(Map<String, String> xsltRegexMap, String destCharset,String method, String version,String charset) throws TransformerConfigurationException, IOException{
		init(xsltRegexMap, destCharset, method, version,charset);
		for (Map.Entry<String, String> entry : xsltRegexMap.entrySet()) {
			File path = new File(entry.getKey());
			final String xsltRegex = entry.getValue();
			for (File xslt : path.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xslt") && name.matches(xsltRegex)) {
						return true;
					} else {
						return false;
					}
				}
			})) {
				transformerList.add(transformerMap.get(xslt.toString()));
			}
		}
	}
	public void transform(File xml, File dest,String charset) throws TransformerException, IOException{
		transform(new FileInputStream(xml),new FileOutputStream(dest),charset);
	}
	public void transform(InputStream xml, OutputStream dest, String xmlCharset) throws TransformerException, UnsupportedEncodingException{
		transform(new InputStreamReader(xml,xmlCharset),dest);
	}
	public void transform(Reader xml, OutputStream dest) throws TransformerException{
		transform(new StreamSource(xml),new StreamResult(dest));
	}
	public void transform(File xml, OutputStream dest,String charset) throws TransformerException, IOException{
		transform(new FileInputStream(xml),dest,charset);
	}
	
	
	
	
	public void transform(org.w3c.dom.Node node, String destpath) throws TransformerException{
		transform(node, new File(destpath));
	}
	public void transform(org.w3c.dom.Node node, File dest) throws TransformerException{
		transform(new DOMSource(node),new StreamResult(dest));
	}
	public void transform(org.w3c.dom.Node node, OutputStream dest) throws TransformerException{
		transform(new DOMSource(node),new StreamResult(dest));
	}
	public void transform(org.w3c.dom.Node node, Writer dest) throws TransformerException{
		transform(new DOMSource(node),new StreamResult(dest));
	}
	private void transform(Source xmlSource, Result result) throws TransformerException{
		for(Transformer transformer:transformerList){
			transformer.transform(xmlSource, result);
		}
	}
	
}
