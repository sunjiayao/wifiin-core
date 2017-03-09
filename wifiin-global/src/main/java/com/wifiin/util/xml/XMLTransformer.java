package com.wifiin.util.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import com.wifiin.common.CommonConstant;
import com.wifiin.util.string.ThreadLocalStringBuilder;


/**
 * 执行xslt，把一段xml文本或xml文件转化成另一种格式
 * */
public class XMLTransformer {
	private String srcCharset;
	private String destCharset;
	public String getSrcCharset() {
		return srcCharset;
	}
	public void setSrcCharset(String srcCharset) {
		this.srcCharset = srcCharset;
	}
	public String getDestCharset() {
		return destCharset;
	}
	public void setDestCharset(String destCharset) {
		this.destCharset = destCharset;
	}
	/**
	 * 创建XMLTransformer对象，以utf8作为输入与输出的字符集
	 */
	public XMLTransformer(){
		this(CommonConstant.DEFAULT_CHARSET_NAME);
	}
	/**
	 * 指定输入与输出的字符集都是<code>charset</code>
	 * @param charset
	 *        输入的xml与输出的xml都是这个值
	 */
	public XMLTransformer(String charset){
		this(charset,charset);
	}
	/**
	 * 
	 * @param srcCharset
	 *        输入的xml字符集
	 * @param destCharset
	 *        输出的xml字符集
	 */
	public XMLTransformer (String srcCharset, String destCharset){
		this.srcCharset=srcCharset;
		this.destCharset=destCharset;
	}
	/**
	 * 根据指定xslt转换指定的xml，并把转换的结果写入指定的Writer
	 * @param Reader xml:    源xml
	 * @param Reader xslt:   要转换的规则
	 * @param Writer dest:   转换的结果
	 * @return 当前XMLTransformer对象
	 * @throws TransformerException 
	 * */
	public XMLTransformer transform(Reader xml, Reader xslt, Writer dest) throws TransformerException{
		try{
			TransformerFactory factory=TransformerFactory.newInstance();//按照我对api的理解，在多线程并发的情况下这个类的对象不能是单例的。
			Source xsltSource=new StreamSource(xslt);
			Transformer transformer=factory.newTransformer(xsltSource);
			
			Source xmlSource=new StreamSource(xml);
			Result result=new StreamResult(dest);
			transformer.setOutputProperty(OutputKeys.ENCODING, this.destCharset);
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.VERSION, "4.0");
			transformer.transform(xmlSource, result);
		}catch(Exception e){
			throw new TransformerException(e.getMessage(),e);
		}
		return this;
	}
	
	/**
	 * @param xmlPath: xml文件的路径
	 * @param xsltPath:xslt文件的路径
	 * @param destPath:保存转换结果的文件路径
	 * @return 当前XMLTransformer对象
	 * @throws IOException 
	 * @throws TransformerException 
	 * */
	public XMLTransformer transform(String xmlPath, String xsltPath, String destPath) throws IOException, TransformerException{
		InputStream xmlIn=null;
		InputStream xsltIn=null;
		OutputStream out=null;
		try{
			xmlIn=new FileInputStream(xmlPath);
			xsltIn=new FileInputStream(xsltPath);
			out=new FileOutputStream(destPath);
			return transform(xmlIn, xsltIn, out);
		}finally{
			if(xmlIn!=null){xmlIn.close();}
			if(xsltIn!=null){xsltIn.close();}
			if(out!=null){out.close();}
		}
	}
	
	
	/**
	 * @param xml: 源xml文件
	 * @param xslt:xslt文件
	 * @param dest:保存转换结果的文件
	 * @return 当前XMLTransformer对象
	 * @throws IOException 
	 * @throws TransformerException 
	 * */
	public XMLTransformer transform(File xml, File xslt, File dest) throws IOException, TransformerException{
		InputStream xmlIn=null;
		InputStream xsltIn=null;
		OutputStream out=null;
		try{
			xmlIn=new FileInputStream(xml);
			xsltIn=new FileInputStream(xslt);
			out=new FileOutputStream(dest);
			return transform(xmlIn, xsltIn, out);
		}finally{
			if(xmlIn!=null){xmlIn.close();}
			if(xsltIn!=null){xsltIn.close();}
			if(out!=null){out.close();}
		}
	}
	
	/**
	 * @param xmlInputStream:   表示源xml的输入流
	 * @param xsltInputStream： 表示xslt的输入流
	 * @param destOutputStream：表示转换目标的输出流
	 * @return 当前XMLTransformer对象
	 * @throws UnsupportedEncodingException 
	 * @throws TransformerException 
	 * @throws TransformException 
	 * */
	public XMLTransformer transform(InputStream xmlInputStream, InputStream xsltInputStream, OutputStream destOutputStream) throws UnsupportedEncodingException, TransformerException{
		Reader xmlReader=new InputStreamReader(xmlInputStream,this.srcCharset);
		Reader xsltReader=new InputStreamReader(xsltInputStream,this.srcCharset);
		Writer destWriter=new OutputStreamWriter(destOutputStream,this.destCharset);
		return transform(xmlReader, xsltReader, destWriter);
	}
	/**
	 * @param xmlNode:   要转换的xml节点对象，可表示一个dom4j Document对象,或一个Element对象
	 * @param xslt：             用于转换的xslt文件
	 * @param dest：            输出转换结果的目标文件
	 * @return 当前XMLTransformer对象
	 * @throws TransformerException
	 * @throws IOException 
	 * */
	public XMLTransformer transform(Node xmlNode, File xslt, File dest) throws TransformerException, IOException{
		Source xmlSource=new DOMSource(xmlNode);
		Source xsltSource=new StreamSource(xslt);
		Result result=new StreamResult(new FileWriter(dest));
		TransformerFactory factory=TransformerFactory.newInstance();
		Transformer transformer=factory.newTransformer(xsltSource);
		transformer.setOutputProperty(OutputKeys.ENCODING, this.destCharset);
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.VERSION, "4.0");
		transformer.transform(xmlSource, result);
		return this;
	}
	/**
	 * @param xmlNode:   要转换的xml节点对象，可表示一个dom4j Document对象,或一个Element对象
	 * @param xslt：             用于转换的xslt的文件路径
	 * @param dest：            输出转换结果的目标文件路径
	 * @return 当前XMLTransformer对象
	 * @throws TransformerException
	 * @throws IOException 
	 * */
	public XMLTransformer transform(Node xmlNode, String xsltPath, String destOutputPath) throws TransformerException, IOException{
		return transform(xmlNode,new File(xsltPath), new File(destOutputPath));
	}
	
	/**
	 * @param xmlNode:   要转换的xml节点对象，可表示一个dom4j Document对象,或一个Element对象
	 * @param xslt：             用于转换的xslt的字符流
	 * @param dest：            输出转换结果的目标字符流
	 * @return 当前XMLTransformer对象
	 * @throws TransformerException
	 * */
	public XMLTransformer transform(Node xmlNode, Reader xslt, Writer dest) throws TransformerException{
		Source xmlSource=new DOMSource(xmlNode);
		Source xsltSource=new StreamSource(xslt);
		Result result=new StreamResult(dest);
		TransformerFactory factory=TransformerFactory.newInstance();
		Transformer transformer=factory.newTransformer(xsltSource);
		transformer.setOutputProperty(OutputKeys.ENCODING, this.destCharset);
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.VERSION, "4.0");
		transformer.transform(xmlSource, result);
		return this;
	}
	/**
	 * @param xmlNode:   要转换的xml节点对象，可表示一个dom4j Document对象,或一个Element对象
	 * @param xslt：             用于转换的xslt的字节流
	 * @param dest：            输出转换结果的目标字节流
	 * @return 当前XMLTransformer对象
	 * @throws TransformerException
	 * */
	public XMLTransformer transform(Node xmlNode, InputStream xslt, OutputStream dest) throws TransformerException{
		Source xmlSource=new DOMSource(xmlNode);
		Source xsltSource=new StreamSource(xslt);
		Result result=new StreamResult(dest);
		TransformerFactory factory=TransformerFactory.newInstance();
		Transformer transformer=factory.newTransformer(xsltSource);
		transformer.setOutputProperty(OutputKeys.ENCODING, this.destCharset);
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.VERSION, "4.0");
		transformer.transform(xmlSource, result);
		return this;
	}
	
	public static final String filepath="filepath";
	public static final String replacedStr="replacedStr";
	/**
	 * @param replaced: 要被替换的源
	 * @param filepathOrReplacedString：取值只有两个：filepath或replacedStr
	 *        如果是filepath表示第一个参数是一个文件路径，要被替换的内容在这个文件里
	 *        如果是replacedStr表示要被替换的内容就是第一个参数本身
	 * @return 替换后的文本
	 * @throws IOException 
	 * */
	public String replaceEntityReference(String replaced, String filepathOrReplacedString) throws IOException{
		if(XMLTransformer.filepath.equals(filepathOrReplacedString)){
			return this.replaceEntityReference(new File(replaced));
		}
		return replaced.replaceAll("&lt;", "<")
		               .replaceAll("&gt;", ">")
		               .replaceAll("&amp;", "&")
		               .replaceAll("&apos;", "'")
		               .replaceAll("&quot;", "\"")
		               .replaceAll("&nbsp;", " ")
		               .replaceAll("(\"\\s+)|(\\s+\")", "\" ")
		               .replaceAll("=\"\\s+", "=\"")
		               .replaceAll("<\\?xml version=\"\\d+(\\.\\d+){0,1}\" encoding=\"(("+destCharset.toLowerCase()+")|("+destCharset.toUpperCase()+"))\"\\?>","")
		               .trim();
	}
	
	/**
	 * @param 要被替换的内容在由参数指定的文件中
	 * @return 替换后的文本
	 * @throws IOException 
	 * */
	public String replaceEntityReference(String filepath) throws IOException{
		return replaceEntityReference(new File(filepath));
	}
	/**
	 * @param 要被替换的内容在由参数指定的文件中
	 * @return 替换后的文本
	 * @throws IOException 
	 * */
	public String replaceEntityReference(File file) throws IOException{
		InputStream in=null;
		OutputStream out=null;
		try{
			in=new FileInputStream(file);
			byte[] read=new byte[(int)file.length()];
			in.read(read);
			String dest=null;
			if(read.length>0){
				dest=replaceEntityReference(new String(read,this.srcCharset),XMLTransformer.replacedStr);
				out=new FileOutputStream(file);
				out.write(dest.getBytes(this.destCharset));
			}
			return dest;
		}finally{
			if(in!=null){in.close();}
			if(out!=null){out.close();}
		}
	}
	
	/**
	 * @param reader: 要被替换的内容在reader中
	 * @param writer：替换结束写出writer
	 * @return 替换后的文本
	 * @throws IOException 
	 * */
	public String replaceEntityReference(Reader reader, Writer writer) throws IOException{
		BufferedReader buffReader=null;
		try{
			StringBuilder replacedBuilder=ThreadLocalStringBuilder.builder().append("");
			buffReader=new BufferedReader(reader);
			String read=null;
			while((read=buffReader.readLine())!=null){
				replacedBuilder.append(read);
			}
			
			String replacedStr=replaceEntityReference(replacedBuilder.toString(),XMLTransformer.replacedStr);
			if(writer!=null){
				writer.write(replacedStr);
			}
			return replacedStr;
		}finally{}
	}

	/**
	 * @param in: 要被替换的内容在in中
	 * @param out：替换结束写出out
	 * @return 替换后的字符串
	 * @throws IOException 
	 * */
	public String replaceEntityReference(InputStream in, OutputStream out) throws IOException{
		Reader reader=new InputStreamReader(in, this.destCharset);
		Writer writer=null;
		if(out!=null){
			writer=new OutputStreamWriter(out, this.destCharset);
		}
		return replaceEntityReference(reader, writer);
	}

}