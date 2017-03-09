package com.wifiin.struts.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.wifiin.common.CommonConstant;
import com.wifiin.common.GlobalObject;
import com.wifiin.exception.ResponseWriteException;
import com.wifiin.exception.ResponseWriteJsonException;
import com.wifiin.exception.ResponseWriteJsonpException;
import com.wifiin.util.Help;
import com.wifiin.util.ip.IPSeeker;
import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * 继承com.opensymphony.xwork2.ActionSupport
 * 提供实现struts2 action通用api
 */
public abstract class AbstractBaseAction extends ActionSupport {

	private static final long serialVersionUID = -4009931842947953218L;
	
	private static final String iframeFn=CommonConstant.getIFRAMEFN();
	
	/**
	 * 操作失败时可用action返回此方法
	 */
	public static final String FAILURE="failure";
	public static final String NOT_GRANTED="notGranted";
	public static final String LOG_SUCCESS="logsuccess";
	/**
	 * struts2把this.result转化成json串响应浏览器,默认的类型，如果action返回ActionSupport.SUCCESS,这就是默认的响应类型
	 */
	public static final String JSON_PROPERTY="json-property";
	/**
	 * 请使用ActionSupport.NONE;
	 */
	public static final String NONE_RESULT="none-result";
	/**
	 * 向浏览器返回简单文本信息
	 */
	public static final String PLAIN_TEXT_PROPERTY="plainText-property";
	/**
	 * 向浏览器返回html文本
	 */
	public static final String HTML_TEXT_RESULT_PROPERTY="htmlText-property";
	/**
	 * 向浏览器返回xml文本
	 */
	public static final String XML_TEXT_RESULT_PROPERTY="xmlText-property";
	/**
	 * 向浏览器返回json文本,this.reuslt本身就是json文本
	 */
	public static final String JSON_TEXT_RESULT_PROPERTY="jsonText-property";
	/**
	 * this.result是一个文件路径,struts2会从这个文件读取字节并响应浏览器
	 */
	public static final String FILEPATH_RESULT_PROPERTY="filepath-property";
	/**
	 * this.result是一个文件对象,struts2会从这个文件读取字节并响应浏览器
	 */
	public static final String FILE_RESULT_PROPERTY="file-property";
	/**
	 * zip Result类型。采用此类型，会指示浏览器以zip格式获取数据。并保存到本地
	 */
	public static final String ZIP_RESULT="zip";
	/**
	 * xml Result类型。采用此类型，会指示浏览器以xml格式获取数据。并保存到本地
	 */
	public static final String XML_RESULT="xml";
	/**
	 * xml文本Result类型。采用此类型，会指示浏览器以xml文本获取数据，不保存到本地
	 */
	public static final String XML_TEXT_RESULT="xmlText-property";
	/**
	 * 未知数据类型的数据获取，由浏览器自己判断
	 */
	public static final String COMMON_DOWNLOAD="common-download";
	
	/**
	 * action结果对象。此类定义的各种Result类型都从此属性获取数据。
	 */
	protected Object result;
	/**
	 * 获得请求的uri
	 * @return String
	 */
	public String getRequestUri() {
		return this.getRequest().getRequestURI();
	}
	/**
	 * 得到发起请求的ip
	 * @return
	 */
	public String getIp(){
		return IPSeeker.getIp(getRequest());
	}
	/**
	 * 获得请求后缀
	 * @return String
	 */
	public String getUriPostfix(){
		String uri=getRequestUri();
		int idx=uri.lastIndexOf('.');
		return idx>=0?uri.substring(idx+1):"";
	}
	/**
	 * 获得path相对于此工程的WebRoot的绝对路径。如果path不是以"/"开头，就自动在path头添加"/"。
	 * 如果path是null或空字符串，就采用"/"
	 * @param path
	 * @return String
	 */
	public String getRealpath(String path){
		if(Help.isEmpty(path)){
			path="/";
		}else if(!path.startsWith("/")){
			path="/"+path;
		}
		return ServletActionContext.getServletContext().getRealPath(path);
	}
	/**
	 * 
	 * @return HttpServletResponse
	 */
	public HttpServletResponse getResponse(){
		return ServletActionContext.getResponse();
	}
	/**
	 * 
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest(){
		return ServletActionContext.getRequest();
	}
	/**
	 * 
	 * @return
	 * @see com.opensymphony.xwork2.ActionSupport#getLocale()
	 */
	public Locale getLocale(){
		return getRequest().getLocale();
	}
	/**
	 * 得到浏览器语言
	 * @return String
	 */
	public String getBrowserLanguage(){
		return getLocale().getDisplayLanguage();
	}
	public boolean containsRequestHeader(String name){
		return Help.isNotEmpty(getRequest().getHeader(name));
	}
	/**
	 * 得到名字是name的请求头信息
	 * @param name
	 * @return String
	 */
	public String getRequestHeader(String name){
		return getRequest().getHeader(name);
	}
	/**
	 * 得到请求头的值，并转化成整数
	 */
	public int getIntRequestHeader(String name, int defaultValue){
		String header=getRequest().getHeader(name);
		return Help.isNotEmpty(header)?Integer.parseInt(header):defaultValue;
	}
	/**
	 * 得到请求头的值，并转化成long型
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public long getLongRequestHeader(String name, long defaultValue){
		String header=getRequest().getHeader(name);
		return Help.isNotEmpty(header)?Long.parseLong(header):defaultValue;
	}
	
	/**
	 * 得到请求头的语言信息: Accept-Language
	 * @return String
	 */
	public String getRequestLanguage(){
		return getRequestHeader("Accept-Language");
	}
	/**
	 * 得到web应用classes目录下的相对路径名称是path的资源输入流
	 * @param path
	 * @return InputStream
	 */
	public InputStream getResourceStream(String path){
		return ServletActionContext.getServletContext().getResourceAsStream(path);
	}
	/**
	 * 得到响应输出字符流
	 * @return PrintWriter
	 * @throws IOException 
	 */
	public PrintWriter getResponseWriter() throws IOException{
		return getResponse().getWriter();
	}
	/**
	 * 得到响应输出字节流
	 * @return OutputStream
	 * @throws IOException 
	 */
	public OutputStream getResponseOutputStream() throws IOException{
		return getResponse().getOutputStream();
	}
	/**
	 * 得到响应输出字符流。并为响应头添加名为headerName，值headerVal的属性
	 * @param headerName
	 * @param headerVal
	 * @return PrintWRiter
	 * @throws IOException 
	 */
	public PrintWriter getResponseWriter(String headerName, String headerVal) throws IOException{
		HttpServletResponse response=getResponse();
		response.addHeader(headerName, headerVal);
		return response.getWriter();
	}
	/**
	 * 得到响应输出字符流。并把header包含的名值对作为响应头属性。
	 * @param header
	 * @return PrintWriter
	 * @throws IOException 
	 * @see
	 */
	public PrintWriter getResponseWriter(Map<String,String> header) throws IOException{
		HttpServletResponse response=getResponse();
		for(Map.Entry<String, String> entry:header.entrySet()){
			response.addHeader(entry.getKey(), entry.getValue());
		}
		return response.getWriter();
	}
	/**
	 * 得到响应输出字节流。并为响应头添加名为headerName，值headerVal的属性
	 * @param headerName
	 * @param headerVal
	 * @return OutputStream
	 * @throws IOException
	 */
	public OutputStream getResponseOutputStream(String headerName, String headerVal) throws IOException{
		HttpServletResponse response=getResponse();
		response.addHeader(headerName, headerVal);
		return response.getOutputStream();
	}
	/**
	 * 得到响应输出字节流。并把header包含的名值对作为响应头属性。
	 * @param header
	 * @return OutputStream
	 * @throws IOException
	 */
	public OutputStream getResponseOutputStream(Map<String, String> header) throws IOException{
		HttpServletResponse response=addHeaders(header);
		return response.getOutputStream();
	}
	/**
	 * 得到响应输出字节流，并且向这个流输出的字节是被zip压缩的。并为响应头添加名为headerName，值headerVal的属性。
	 * @param headerName
	 * @param headerVal
	 * @return OutputStream
	 * @throws IOException
	 */
	public ZipOutputStream getZipedResponseOutputStream(String headerName, String headerVal) throws IOException{
		HttpServletResponse response=getResponse();
		response.addHeader(headerName, headerVal);
		response.addHeader("Content-Type", "application/zip,application/x-zip-compressed");
		return createZipedResponseOutputStream(response);
	}
	/**
	 * 得到响应输出字节流，并且向这个流输出的字节是被zip压缩的。并把header包含的名值对作为响应头属性。
	 * @param header
	 * @return OutputStream
	 * @throws IOException
	 */
	public ZipOutputStream getZipedResponseOutputStream(Map<String, String> header) throws IOException{
		header.put("Content-Type", "application/zip,application/x-zip-compressed");
		return createZipedResponseOutputStream(addHeaders(header));
	}
	
	private HttpServletResponse addHeaders(Map<String, String> header){
		HttpServletResponse response=getResponse();
		for(Map.Entry<String, String> entry:header.entrySet()){
			response.addHeader(entry.getKey(), entry.getValue());
		}
		return response;
	}
	/**
	 * 从response得到输出流并用ZipOutputStream封装
	 * @param response
	 * @return ZipOutputStream
	 * @throws IOException 
	 */
	public ZipOutputStream createZipedResponseOutputStream(HttpServletResponse response) throws IOException{
		return new ZipOutputStream(response.getOutputStream());
	}
	/**
	 * 把content写入响应输出流，并把header包含的名值对作为响应头信息
	 * @param content
	 * @param header
	 */
	public void write(String content,Map<String,String> header){
		HttpServletResponse response=getResponse();
		for(Map.Entry<String, String> entry:header.entrySet()){
			response.addHeader(entry.getKey(), entry.getValue());
		}
		write(content);
	}
	/**
	 * 把content写入响应输出流。并把headerName和headerValue作为响应头的属性名和值
	 * @param content
	 * @param headerName
	 * @param headerValue
	 */
	public void write(String content, String headerName, String headerValue){
		getResponse().addHeader(headerName,headerValue);
		write(content);
	}
	/**
	 * 把content输出到响应输出流
	 * @param content
	 */
	public void write(String content){
		try{
			PrintWriter pw=getResponseWriter();
			pw.write(content);
			pw.flush();
		}catch(Exception e){
			throw new ResponseWriteException(e);
		}
	}
	/**
	 * 把data转化成json，并输出到响应输出流，采用默认字符集utf8
	 * @param data
	 */
	public void writeJson(Object data){
		writeJson(data,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	/**
	 * 把data转化成json，并以charset指定的字符集输出到响应输出流
	 * @param data
	 * @param charset
	 */
	public void writeJson(Object data, String charset){
		try{
			write(GlobalObject.getJsonMapper().writeValueAsString(data),"Content-Type", "text/json;charset="+charset);
		}catch(Exception e){
			throw new ResponseWriteJsonException(e);
		}
	}
	/**
	 * 简单jsonp支持
	 * jsfname是要返回给浏览器的js函数名，必须是在当前js页面内沿不存在的js函数
	 * */
	public void writeJsonp(Object data,String jsfname){
		writeJsonp(data,jsfname,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	/**
	 * 简单jsonp支持
	 * jsfname是要返回给浏览器的js函数名，必须是在当前js页面内沿不存在的js函数
	 * */
	public void writeJsonp(Object data, String jsfname, String charset){
		try{
			write(ThreadLocalStringBuilder.builder().append("var ").append(jsfname)
					.append("=function(){return ")
					.append(GlobalObject.getJsonMapper().writeValueAsString(data))
					.append(";}")
					.toString(),"Content-Type","application/x-javascript;charset="+charset);
		}catch(Exception e){
			throw new ResponseWriteJsonpException(e);
		}
	}
	private int scriptCount=0;
	
	/**
	 * 把arg用<script></script>包含并输出到响应输出流，采用默认字符集utf8
	 * @param arg
	 */
	public void writeIframe(String arg){
		writeIframe(arg, CommonConstant.DEFAULT_CHARSET_NAME);
	}
	/**
	 * 把arg用<script></script>包含并输出到响应输出流，采用charset指定的字符集
	 * @param arg
	 * @param charset
	 */
	public void writeIframe(String arg, String charset){
		String id="scriptId"+(scriptCount++);
		write("<script id=\""+id+"\">(function(){parent."+iframeFn+"('"+arg+"');var script=document.getElementById('"+id+"');script.parentNode.removeChild(script);script=null;})();</script>","Content-Type", "text/html;charset="+charset);
	}
	/**
	 * 把data转化成json，再用<script></script>包含，最后输出到响应输出流，采用默认字符集utf8
	 * @param data
	 */
	public void writeJsonToIframe(Object data){
		writeJsonToIframe(data,CommonConstant.DEFAULT_CHARSET_NAME);
	}
	/**
	 * 把data转化成json，再用<script></script>包含，最后输出到响应输出流，采用charset指定的字符集
	 * @param data
	 */
	public void writeJsonToIframe(Object data, String charset){
		try{
			writeIframe(GlobalObject.getJsonMapper().writeValueAsString(data),charset);
		}catch(Exception e){
			throw new ResponseWriteException(e);
		}
	}

	/**
	 * 得到名字是name的cookie值
	 * @param name
	 * @return String
	 */
	public String getCookieValue(String name){
		Cookie[] cs=getRequest().getCookies();
		if(Help.isNotEmpty(cs)){
			for(int i=0,l=cs.length;i<l;i++){
				Cookie c=cs[i];
				if(c.getName().equals(name)){
					return c.getValue();
				}
			}
		}
		return null;
	}
	/**
	 * 得到名字是name的cookie值，并转化成整型。如果不存在此cookie返回null，如果此cookie不是数字会抛出异常
	 * @param name
	 * @return Integer
	 */
	public Integer getIntCookieValue(String name){
		String v=getCookieValue(name);
		if(v!=null){
			return Integer.parseInt(v);
		}else{
			return null;
		}
	}
	/**
	 * 得到名字是name的cookie值，并转化成长整型。如果不存在此cookie返回null，如果此cookie不是数字会抛出异常
	 * @param name
	 * @return Long
	 */
	public Long getLongCookieValue(String name){
		String v=getCookieValue(name);
		if(v!=null){
			return Long.parseLong(v);
		}else{
			return null;
		}
	}
	/**
	 * 添加名字是name，值是value的cookie
	 * @param name
	 * @param value
	 */
	public void addCookie(String name, String value){
		getResponse().addCookie(new Cookie(name,value));
	}
	/**
	 * 得到此次action处理的结果对象
	 * @return Object
	 */
	public Object getResult() {
		return result;
	}
	/**
	 * @param result 此次action处理的结果对象
	 */
	public void setResult(Object result) {
		this.result = result;
	}
}
