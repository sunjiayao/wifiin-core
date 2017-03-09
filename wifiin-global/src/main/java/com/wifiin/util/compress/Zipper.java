package com.wifiin.util.compress;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.wifiin.common.CommonConstant;

public class Zipper {
	private ZipOutputStream zipout;
	private String charset=CommonConstant.DEFAULT_CHARSET_NAME;
	private PrintWriter pw;
	public Zipper(){}
	public Zipper(OutputStream target){
		this.zipout=new ZipOutputStream(target);
	}
	public Zipper(OutputStream target, String charset) throws UnsupportedEncodingException{
		this(target);
		this.charset=charset;
		pw=new PrintWriter(new OutputStreamWriter(zipout,charset));
	}

	public void addZipEntry(String name) throws IOException{
		zipout.putNextEntry(new ZipEntry(name));
	}
	public void closeEntry() throws IOException{
		zipout.closeEntry();
	}

	/**
	 * 向zip输出流追加一行内容，内容是src
	 * @param src void 要压缩的内容
	 * @throws 
	 * @exception
	 */
	public void zipLine(String src){
		pw.println(src);
	}
	public void zipEmptyLine(){
		pw.println();
	}
	/**
	 * 向zip输出流追加内容，内容就是src
	 * @param src:要压缩的内容
	 * */
	public void zip(String src) throws UnsupportedEncodingException, IOException{
		this.zipout.write(src.getBytes(this.charset));
	}
	/**
	 * 调用一次添加一个zip条目
	 * @param name:条目名
	 * @param src:要压缩的内容
	 * */
	public void zip(String name, String src) throws UnsupportedEncodingException, IOException{
		this.addZipEntry(name);
		this.zip(src);
		this.closeEntry();
	}
	/**
	 * 向zip输出流追加内容，内容来自reader
	 * @param reader:要压缩的内容
	 * */
	public void zip(Reader reader) throws IOException{
		BufferedReader br=new BufferedReader(reader);
		String l=null;
		while((l=br.readLine())!=null){
			this.zipLine(l);
		}
	}
	/**
	 * 调用一次添加一个zip条目
	 * @param name:条目名字
	 * @param src:要压缩的内容
	 * @throws IOException 
	 * 
	 * */
	public void zip(String name, Reader reader) throws IOException{
		this.addZipEntry(name);
		this.zip(reader);
		this.closeEntry();
	}
	/**
	 * 向zip输出流追加内容，内容来自src
	 * @param src:要压缩的内容
	 * */
	public void zip(byte[] src) throws IOException{
		zipout.write(src);
	}
	/**
	 * 调用一次添加一个zip条目
	 * @param name:条目的名字
	 * @param src:要压缩的内容
	 * @throws IOException 
	 * 
	 * */
	public void zip(String name, byte[] src) throws IOException{
		this.addZipEntry(name);
		this.zip(src);
		this.closeEntry();
	}
	/**
	 * 向zip输出流追加内容，内容是src从off开始len长度的字节
	 * @param src:要压缩的内容
	 * @param off:要压缩的内容在数据的开始索引
	 * @param len:要压缩的字节数
	 * */
	public void zip(byte[] src, int off, int len) throws IOException{
		zipout.write(src, off, len);
	}
	/**
	 * 调用一次添加一个zip条目
	 * @param name:条目的名字
	 * @param src:要压缩的内容
	 * @param off:要压缩的内容在数据的开始索引
	 * @param len:要压缩的字节数
	 * */
	public void zip(String name, byte[] src, int off, int len) throws IOException{
		this.addZipEntry(name);
		this.zip(src, off, len);
		this.closeEntry();
	}
	/**
	 * 向zip输出流追加内容，内容来自in
	 * @param int:要压缩的内容
	 * */
	public void zip(InputStream in) throws IOException{
		byte[] buf=new byte[8192];
		int c=-1;
		while((c=in.read(buf))>-1){
			zip(buf,0,c);
		}
	}
	/**
	 * 调用一次添加一个zip条目
	 * @param name:条目的名字
	 * @param in:要压缩的内容
	 * @throws IOException 
	 * */
	public void zip(String name, InputStream in) throws IOException{
		this.addZipEntry(name);
		this.zip(in);
		this.closeEntry();
	}
	public void zipFile(File tozip) throws IOException{
		Zipper.zip(tozip, zipout);
	}
	public void zipFiles(File[] tozip) throws IOException{
		Zipper.zip(tozip, zipout);
	}

	public void flush() throws IOException{
		pw.flush();
		zipout.flush();
	}
	public void close() throws IOException{
		if(pw!=null){
			pw.close();
		}
	}
	
	
	public static void unzip(String zipParent, String[] zip, String target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target);
		}
	}
	public static void unzip(String zipParent, String[] zip, String target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target,filter);
		}
	}
	public static void unzip(File zipParent, String[] zip, String target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target);
		}
	}
	public static void unzip(File zipParent, String[] zip, String target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target,filter);
		}
	}
	public static void unzip(File zipParent, String[] zip, File target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target);
		}
	}
	public static void unzip(File zipParent, String[] zip, File target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target, filter);
		}
	}
	public static void unzip(String zipParent, String[] zip, File target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target);
		}
	}
	public static void unzip(String zipParent, String[] zip, File target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(new File(zipParent,zip[i]),target, filter);
		}
	}
	public static void unzip(File[] zip, String target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target);
		}
	}
	public static void unzip(File[] zip, String target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target, filter);
		}
	}
	public static void unzip(String[] zip, File target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target);
		}
	}
	public static void unzip(String[] zip, File target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target, filter);
		}
	}
	public static void unzip(File[] zip, File target) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target);
		}
	}
	public static void unzip(File[] zip, File target, FilenameFilter filter) throws ZipException, IOException{
		for(int i=0,l=zip.length;i<l;i++){
			unzip(zip[i],target, filter);
		}
	}
	public static void unzip(File zip, String target) throws ZipException, IOException{
		unzip(zip,new File(target));
	}
	public static void unzip(File zip, String target, FilenameFilter filter) throws ZipException, IOException{
		unzip(zip,new File(target),filter);
	}
	public static void unzip(String zip, File target) throws ZipException, IOException{
		unzip(new File(zip),target);
	}
	public static void unzip(String zip, File target, FilenameFilter filter) throws ZipException, IOException{
		unzip(new File(zip),target, filter);
	}
	public static void unzip(String zip, String target) throws ZipException, IOException{
		unzip(new File(zip),new File(target));
	}
	public static void unzip(String zip, String target, FilenameFilter filter) throws ZipException, IOException{
		unzip(new File(zip),new File(target),filter);
	}
	public static void unzip(File zip, File target) throws ZipException, IOException{
		unzip(zip,target,new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		});
	}
	@SuppressWarnings("unchecked")
	public static void unzip(File zip, File target, FilenameFilter filter) throws ZipException, IOException{
		if(zip.exists()){
			if((target.exists() && target.isDirectory()) || target.mkdirs()){
				ZipFile zf=null;
				try{
					zf=new ZipFile(zip);
					Enumeration<ZipEntry> entries=(Enumeration<ZipEntry>)zf.entries();
					while(entries.hasMoreElements()){
						ZipEntry ze=entries.nextElement();
						String zn=ze.getName();
						if(filter.accept(null, zn)){
							unzip(zf,ze,new File(target,zn));
						}
					}
				}finally{
					if(zf!=null){
						zf.close();
					}
				}
			}
		}else{
			throw new FileNotFoundException();
		}
	}
	
	private static boolean unzip(ZipFile zip, ZipEntry entry, File target) throws IOException{
		boolean exists=target.exists();
		if(entry.isDirectory()){
			if(!exists){
				target.mkdirs();
			}
			return true;
		}
		if(!exists){
			File parentFile=target.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
			target.createNewFile();
		}
		BufferedOutputStream out=null;
		if(target.isFile()){
			try{
				out=new BufferedOutputStream(new FileOutputStream(target));
				InputStream in=zip.getInputStream(entry);
				byte[] buf=new byte[8192];
				int len=-1;
				while((len=in.read(buf))>0){
					out.write(buf, 0, len);
				}
			}finally{
				if(out!=null){
					out.close();
				}
			}
		}
		return exists;
	}


	/**
	 * 对文件进行压缩
	 * @param files 要压缩的文件
	 * @param out 输入流
	 * @throws IOException void
	 * @throws 
	 * @exception
	 */
	public static void zip(String[] files, OutputStream out) throws IOException{
		File[] tozip=new File[files.length];
		ZipOutputStream zipout=null;
		try{
			zipout=new ZipOutputStream(out);
			for(int i=0,l=files.length;i<l;i++){
				zip(new File(files[i]),zipout);
			}
		}finally{
			if(zipout!=null){
				zipout.close();
			}
		}
		zip(tozip,out);
	}
	public static void zip(File[] files,OutputStream out) throws IOException{
		ZipOutputStream zipout=null;
		try{
			zipout=new ZipOutputStream(out);
			for(int i=0,l=files.length;i<l;i++){
				zip(files[i],zipout);
			}
		}finally{
			if(zipout!=null){
				zipout.close();
			}
		}
	}
	public static void zip(String tozipFile, OutputStream out) throws IOException{
		zip(new File(tozipFile),out);
	}
	public static void zip(File tozip, OutputStream out) throws IOException{
		zip(tozip,new ZipOutputStream(out));
	}
	public static void zip(String tozipFile, ZipOutputStream zipout) throws IOException{
		zip(new File(tozipFile),zipout);
	}
	public static void zip(File tozip, ZipOutputStream zipout) throws IOException{
//		if(tozip.isFile()){
			zip(tozip.getName(),new FileInputStream(tozip),zipout);
//		}else{
//			zipout.putNextEntry(new ZipEntry(tozip.getName()+"/"));
//			zipout.closeEntry();
//			zip(tozip.listFiles(),zipout);
//		}
	}
	public static void zip(String name, InputStream src, ZipOutputStream zipout) throws IOException{
		zipout.putNextEntry(new ZipEntry(name));
		byte[] buf=new byte[8192];
		for(int read=0;(read=src.read(buf))>0;){
			zipout.write(buf,0,read);
		}
		zipout.closeEntry();
	}
}
