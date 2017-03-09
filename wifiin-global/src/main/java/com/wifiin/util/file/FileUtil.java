package com.wifiin.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wifiin.util.Help;

/**
 * delete
 * commitSvn
 * updateSvn
 * unzip
 * zip
 * detectCharset  检查文件的字符集
 * loadTxt           把整个文件读入内存
 * loadBytes         把整个文件读入内存
 * */
public class FileUtil {
	/**
	 * 根据文件或目录名称对文件或目录的删除,如果是目录删除目录下的全部文件
	 * @param f void 删除文件或目录的名称
	 * @throws 
	 * @exception
	 */
	public static void delete(String f){
		delete(new File(f));
	}
	/**
	 * 根据文件或目录的File对象删除文件或目录,如果是目录删除目录下的全部文件
	 * @param f void 删除文件或目录的File对象
	 * @throws 
	 * @exception
	 */
	public static void delete(File f){
		if(f.exists()){
			if(f.isDirectory()){
				delete(f.listFiles());
			}
			f.delete();
		}
	}
	/**
	 * 根据文件或目录对象删除文件,如果是目录删除目录下的全部文件
	 * @param filelist void 文件对象数组
	 * @throws 
	 * @exception
	 */
	public static void delete(File[] filelist){
		for(int i=0,l=filelist.length;i<l;i++){
			delete(filelist[i]);
		}
	}
	/**
	 * 根据文件或目录名称数组删除文件或目录,,如果是目录删除目录下的全部文件
	 * @param filelist void 文件或目录名称数组
	 * @throws 
	 * @exception
	 */
	public static void delete(String[] filelist){
		for(int i=0,l=filelist.length;i<l;i++){
			File f=new File(filelist[i]);
			delete(f);
		}
	}
	/**
	 * 根据文件或目录名称数组删除指定路径下的文件或目录
	 * @param basepath File对象，包含文件或目录路径
	 * @param filelist void 删除文件或目录数组
	 * @throws 
	 * @exception
	 */
	public static void delete(File basepath, String[] filelist) {
		if(basepath==null){
			for(int i=0,l=filelist.length;i<l;i++){
				File f=new File(filelist[i]);
				delete(f);
			}
		}else{
			for(int i=0,l=filelist.length;i<l;i++){
				File f=new File(basepath,filelist[i]);
				delete(f);
			}
		}
	}
	/**
	 * 根据文件或目录名称数组删除指定路径下的文件或目录,路径可以为空
	 * @param basepath
	 * @param filelist void
	 * @throws 
	 * @exception
	 */
	public static void delete(String basepath, String[] filelist) {
		if(Help.isEmpty(basepath)){
			delete(filelist);
		}else{
			delete(new File(basepath),filelist);
		}
	}
   /**
    *  根据指定的路径名创建File对象，如果此文件不存在则创建新文件
    * @param file 文件或目录相对或绝对路径
    * @return
    * @throws IOException File
    * @throws 
    * @exception
    */
	public static File getFile(String file) throws IOException{
		File f=new File(file);
		if(!f.exists()){
			f.createNewFile();
		}
		return f;
	}

	/**
	 * 读取文件的内容
	 * @param file 文件名称
	 * @param charset 编码 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException String
	 * @throws 
	 * @exception
	 */
	public static String loadTxt(String file, String charset) throws UnsupportedEncodingException, IOException {
		return loadTxt(new File(file), charset);
	}
	/**
	 * 读取文件的内容
	 * @param file File对象
	 * @param charset 编码
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException String
	 * @throws 
	 * @exception
	 */
	public static String loadTxt(File file, String charset) throws UnsupportedEncodingException, IOException {
		return new String(loadBytes(file),charset);
	}
	/**
	 * 读取文件内容到字节数组
	 * @param file 文件名称
	 * @return
	 * @throws IOException byte[]
	 * @throws 
	 * @exception
	 */
	public static byte[] loadBytes(String file) throws IOException {
		return loadBytes(new File(file));
	}
	/**
	 * 读取文件内容到字节数组
	 * @param file File对象
	 * @return
	 * @throws IOException byte[]
	 * @throws 
	 * @exception
	 */
	public static byte[] loadBytes(File file) throws IOException{
		FileInputStream in=null;
		try{
			in=new FileInputStream(file);
			int len=(int)file.length();
			byte[] buf=new byte[len];
			in.read(buf, 0, len);
			return buf;
		}finally{
			if(in!=null){
				in.close();
			}
		}
	}
}