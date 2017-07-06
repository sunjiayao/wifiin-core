package com.wifiin.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;

public class IOUtil {
    private static final int BUF_SIZE=256;
    private static final ThreadLocal<BufData> BUF_POOL=new ThreadLocal<>();
    private static class BufData{
        public byte[] buf=new byte[BUF_SIZE];
        public ByteArrayOutputStream stream=new ByteArrayOutputStream(BUF_SIZE);;
    }
    private static BufData buf(){
        BufData buf=BUF_POOL.get();
        if(buf==null){
            buf=new BufData();
            BUF_POOL.set(buf);
        }
        return buf;
    }
    public static byte[] read(InputStream in) throws IOException{
        BufData data=buf();
        ByteArrayOutputStream buf=data.stream;
        byte[] bs=data.buf;
        int l=0;
        while((l=in.read(bs))>=0){
            if(l>0){
                buf.write(bs,0,l);
            }
        }
        buf.close();
        try{
            return buf.toByteArray();
        }finally{
            buf.reset();
        }
    }
    public static byte[] read(InputStream in,int len) throws IOException{
        byte[] buf=new byte[len];
        int offset=0;
        int quantity=0;
        while(offset<len && quantity>=0){
            quantity=in.read(buf,offset,buf.length-offset);
            if(quantity>0){
                offset+=quantity;
            }
        }
        return buf;
    }
	public static String readString(InputStream in, String charset) throws IOException{
		byte[] bs=read(in);
		String string=null;
		if(bs!=null && bs.length>0){
			string=new String(bs,Help.isEmpty(charset)?"UTF-8":charset);
		}
		return string;
	}
	public static void write(File file,byte[] content) throws FileNotFoundException, IOException{
	    try(OutputStream out=new FileOutputStream(file)){
	        write(out,content);
	    }
	}
	public static void write(OutputStream out,byte[] content) throws IOException{
	    out.write(content);
	    out.flush();
	}
	public static void write(File file,byte[] content,int offset,int len) throws FileNotFoundException, IOException{
	    try(OutputStream out=new FileOutputStream(file)){
            write(out,content,offset,len);
        }
	}
	public static void write(OutputStream out,byte[] content,int offset,int len) throws IOException{
	    out.write(content,offset,len);
	    out.flush();
	}
	public static void write(File file,String content) throws FileNotFoundException{
	    try(PrintStream ps=new PrintStream(file)){
	        ps.print(content);
	    }
	}
	public static void write(Writer writer,String content) throws IOException{
	    writer.write(content);
	    writer.flush();
	}
	/**
     * 从指定文件读取全部文本内容
     * @param src
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String loadFileText(File src) throws UnsupportedEncodingException, IOException{
        return loadFileText(src,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 从指定文件读取全部文本内容
     * @param src
     * @param charset
     * @return
     * @throws IOException
     */
    public static String loadFileText(File src,Charset charset) throws IOException{
        return new String(loadFileBytes(src),charset);
    }
    /**
     * 从指定文件读取全部文本内容
     * @param src
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String loadFileText(File src, String charset) throws UnsupportedEncodingException, IOException{
        return new String(loadFileBytes(src),charset);
    }
    /**
     * 从指定文件读取全部字节
     * @param src
     * @return
     * @throws IOException
     */
    public static byte[] loadFileBytes(File src) throws IOException{
        return loadFileBytes(src.toPath());
    }
    /**
     * 从指定文件读取全部文本内容
     * @param src
     * @param charset
     * @return
     * @throws IOException
     */
    public static String loadFileText(Path src, Charset charset) throws IOException{
        return new String(loadFileBytes(src),charset);
    }
    /**
     * 从指定文件读取全部文本内容
     * @param src
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String loadFileText(Path src,String charset) throws UnsupportedEncodingException, IOException{
        return new String(loadFileBytes(src),charset);
    }
    /**
     * 从指定文件读取全部文本内容
     * @param src
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String loadFileText(Path src) throws UnsupportedEncodingException, IOException{
        return loadFileText(src,CommonConstant.DEFAULT_CHARSET_NAME);
    }
    /**
     * 从指定文件读取全部字节
     * @param src
     * @return
     * @throws IOException
     */
    public static byte[] loadFileBytes(Path src) throws IOException{
        return Files.readAllBytes(src);
    }
    /**
     * 从reader加载Properties数据
     * @param reader 字符流
     * @return
     * @throws IOException Properties
     * @throws 
     * @exception
     */
    public static Properties loadProperties(Reader reader) throws IOException{
        Properties props=new Properties();
        props.load(reader);
        return props;
    }
    /**
     * 以默认字符集从in加载Properties
     * @param in 字节流
     * @return
     * @throws IOException Properties
     * @throws 
     * @exception
     */
    public static Properties loadProperties(InputStream in) throws IOException{
        Properties props=new Properties();
        props.load(in);
        return props;
    }
    /**
     * 以charset指定的字符集从in加载Properties
     * @param in
     * @param charset
     * @return
     * @throws IOException Properties
     * @throws 
     * @exception
     */
    public static Properties loadProperties(InputStream in, String charset) throws IOException{
        return loadProperties(new InputStreamReader(in, charset));
    }
    /**
     * 从file加载Properties
     * @param file 保存着Properties数据的文件
     * @return 从文件加载的Properties对象
     * @throws FileNotFoundException
     * @throws IOException Properties
     */
    public static Properties loadProperties(File file) throws FileNotFoundException, IOException{
        InputStream in=null;
        try{
            in=new FileInputStream(file);
            return loadProperties(in);
        }finally{
            if(in!=null){
                in.close();
            }
        }
    }
    /**
     * 从file以charset指定字符集加载Properties
     * @param file 保存着Properies数据的文件
     * @param charset file的字符集
     * @return 从file加载的Properties对象
     * @throws IOException Properties
     */
    public static Properties loadProperties(File file, String charset) throws IOException{
        InputStream in=null;
        try{
            in=new FileInputStream(file);
            return loadProperties(in,charset);
        }finally{
            if(in!=null){
                in.close();
            }
        }
    }
    /**
     * 将properties写入writer
     * @param properties
     * @param writer
     * @throws IOException
     */
    public static void storeProperties(Properties properties,Writer writer) throws IOException{
        properties.store(writer,null);
    }
    /**
     * 以默认字符集将properties写入输出流
     * @throws IOException 
     * */
    public static void storeProperties(Properties properties, OutputStream out) throws IOException{
        properties.store(out, null);
    }
    /**
     * 将properties写入out，字符集是charset
     * @param properties
     * @param out
     * @param charset
     * @throws IOException
     */
    public static void storeProperties(Properties properties, OutputStream out, String charset) throws IOException{
        properties.store(new OutputStreamWriter(out, charset), null);
    }
}
