package com.wifiin.util.ip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wifiin.config.ConfigManager;
import com.wifiin.exception.IllegalIPFileException;
import com.wifiin.util.Help;

public class IPSeeker {
	public static final String LOCALHOST="127.0.0.1";
	public static final String bad_ip_file="IP地址库文件错误";
	public static final String unknown_country="未知国家";
	public static final String unknown_area="未知地区";
	
	// 一些固定常量，比如记录长度等等
	private static final int IP_RECORD_LENGTH = 7;
	private static final byte REDIRECT_MODE_1 = 0x01;
	private static final byte REDIRECT_MODE_2 = 0x02;
	/**
	 * 在constant.properties定义的ip地址库文件名的key
	 */
	public static final String IPDAT_NAME="wifiin.ipseeker.ipdat.name";
	// 用来做为cache，查询一个ip时首先查看cache，以减少不必要的重复查找
	private static final Cache<String,IPLocation> ipCache=CacheBuilder.newBuilder().build();
	
	private long ipSrcLastChanged;
	private File ipSrc;
	// 随机文件访问类
	private RandomAccessFile ipFile;
	// 内存映射文件
	private MappedByteBuffer mbb;
	// 起始地区的开始和结束的绝对偏移
	private long ipBegin;
	private long ipEnd;
	private byte[] b3,b4;
	private ByteArrayOutputStream bout;
	
	private IPSeeker(String srcName) throws FileNotFoundException, URISyntaxException  {
		this(new File(IPSeeker.class.getResource("/"+srcName).toURI()));
	}
	private IPSeeker(File ipSrc) throws FileNotFoundException{
		this.ipSrc=ipSrc;
		ipSrcLastChanged=ipSrc.lastModified();
		ipFile=new RandomAccessFile(ipSrc,"r");
		bout=new ByteArrayOutputStream();
		b3=new byte[3];
		b4=new byte[4];
		// 如果打开文件成功，读取文件头信息
		if(ipFile != null) {
			try {
				ipBegin = readLong4(0);
				ipEnd = readLong4(4);
				if(ipBegin == -1 || ipEnd == -1) {
					ipFile.close();
					throw new IllegalIPFileException();
				}
			} catch (IOException e) {
				throw new IllegalIPFileException(e.getMessage(),e);
			}			
		}
	}
	
	
	/**
	 * 给定一个地点的不完全名字，得到一系列包含s子串的IP范围记录
	 * @param s 地点子串
	 * @return 包含IPEntry类型的List
	 * @throws IOException 
	 */
	public List<IPEntry> getIPEntriesDebug(String s) throws IOException {
	    List<IPEntry> ret = new ArrayList<IPEntry>();
	    long endOffset = ipEnd + 4;
	    for(long offset = ipBegin + 4; offset <= endOffset; offset += IP_RECORD_LENGTH) {
	        // 读取结束IP偏移
	        long temp = readLong3(offset);
	        // 如果temp不等于-1，读取IP的地点信息
	        if(temp != -1) {
	            IPLocation ipLoc = getIPLocation(temp);
	            // 判断是否这个地点里面包含了s子串，如果包含了，添加这个记录到List中，如果没有，继续
	            if(ipLoc.getCountry().indexOf(s) != -1 || ipLoc.getArea().indexOf(s) != -1) {
	                IPEntry entry = new IPEntry();
	                entry.country = ipLoc.getCountry();
	                entry.area = ipLoc.getArea();
	                // 得到起始IP
	    	        readIP(offset - 4, b4);
	                entry.beginIp = Help.getIpStringFromBytes(b4);
	                // 得到结束IP
	                readIP(temp, b4);
	                entry.endIp = Help.getIpStringFromBytes(b4);
	                // 添加该记录
	                ret.add(entry);
	            }
	        }
	    }
	    return ret;
	}
	
	/**
	 * 给定一个地点的不完全名字，得到一系列包含s子串的IP范围记录
	 * @param s 地点子串
	 * @return 包含IPEntry类型的List
	 * @throws IOException 
	 */
	public List<IPEntry> getIPEntries(String s) throws IOException {
	    List<IPEntry> ret = new ArrayList<IPEntry>();
        // 映射IP信息文件到内存中
        if(mbb == null) {
		    FileChannel fc = ipFile.getChannel();
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, ipFile.length());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
        }
        
	    int endOffset = (int)ipEnd;
        for(int offset = (int)ipBegin + 4; offset <= endOffset; offset += IP_RECORD_LENGTH) {
            int temp = readInt3(offset);
            if(temp != -1) {
	            IPLocation ipLoc = getIPLocation(temp);
	            // 判断是否这个地点里面包含了s子串，如果包含了，添加这个记录到List中，如果没有，继续
	            if(ipLoc.getCountry().indexOf(s) != -1 || ipLoc.getArea().indexOf(s) != -1) {
	                IPEntry entry = new IPEntry();
	                entry.country = ipLoc.getCountry();
	                entry.area = ipLoc.getArea();
	                // 得到起始IP
	    	        readIP(offset - 4, b4);
	                entry.beginIp = Help.getIpStringFromBytes(b4);
	                // 得到结束IP
	                readIP(temp, b4);
	                entry.endIp = Help.getIpStringFromBytes(b4);
	                // 添加该记录
	                ret.add(entry);
	            }
            }
        }
        return ret;
	}

	/**
	 * 从内存映射文件的offset位置开始的3个字节读取一个int
	 * @param offset
	 * @return
	 */
	private int readInt3(int offset) {
	    mbb.position(offset);
	    return mbb.getInt() & 0x00FFFFFF;
	}

	/**
	 * 从内存映射文件的当前位置开始的3个字节读取一个int
	 * @return
	 */
	private int readInt3() {
	    return mbb.getInt() & 0x00FFFFFF;
	}
	
	/**
	 * 根据IP得到国家名
	 * @param ip ip的字节数组形式
	 * @return 国家名字符串
	 * @throws IOException 
	 * @throws ExecutionException 
	 */
	public String getCountry(byte[] ip) throws ExecutionException {
		// 保存ip，转换ip字节数组为字符串形式
		return getCountry(Help.getIpStringFromBytes(ip));
	}
	/**
	 * 根据ip得到地理位置
	 * @param ip
	 * @return
	 * @throws IOException 
	 * @throws ExecutionException 
	 */
	public IPLocation getIPLocation(String ip) throws ExecutionException{
		return ipCache.get(ip,()->{
		    return getIPLocation(Help.getIpByteArrayFromString(ip));
		});
	}
	/**
	 * 根据IP得到国家名
	 * @param ip IP的字符串形式
	 * @return 国家名字符串
	 * @throws IOException 
	 * @throws ExecutionException 
	 */
	public String getCountry(String ip) throws ExecutionException{
		return getIPLocation(ip).getCountry();
	}

	
	/**
	 * 根据IP得到地区名
	 * @param ip ip的字节数组形式
	 * @return 地区名字符串
	 * @throws ExecutionException 
	 * @throws IOException 
	 */
	public String getArea(byte[] ip) throws IOException, ExecutionException {
		return getArea(Help.getIpStringFromBytes(ip));
	}
	
	/**
	 * 根据IP得到地区名
	 * @param ip IP的字符串形式
	 * @return 地区名字符串
	 * @throws IOException 
	 * @throws ExecutionException 
	 */
	public String getArea(String ip) throws ExecutionException {
	    return getIPLocation(ip).getArea();
	}
	
	public String getProvince(String ip) throws ExecutionException{
		return getIPLocation(ip).getProvince();
	}
	
	public String getProvince(byte[] ip) throws ExecutionException{
		return getProvince(Help.getIpStringFromBytes(ip));
	}
	
	public String getCity(String ip) throws ExecutionException{
		return getIPLocation(ip).getCity();
	}
	
	public String getCity(byte[] ip) throws ExecutionException{
		return getCity(Help.getIpStringFromBytes(ip));
	}
	
	/**
	 * 根据ip搜索ip信息文件，得到IPLocation结构，所搜索的ip参数从类成员ip中得到
	 * @param ip 要查询的IP
	 * @return IPLocation结构
	 * @throws IOException 
	 */
	private IPLocation getIPLocation(byte[] ip) throws IOException {
		IPLocation info = null;
		long offset = locateIP(ip);
		if(offset != -1)
			info = getIPLocation(offset);
		if(info == null) {
			info = new IPLocation();
			info.setCountry (unknown_country);
			info.setArea(unknown_area);
		}
		return info;
	}

	/**
	 * 从offset位置读取4个字节为一个long，因为java为big-endian格式，所以没办法
	 * 用了这么一个函数来做转换
	 * @param offset
	 * @return 读取的long值，返回-1表示读取文件失败
	 */
	private long readLong4(long offset) {
		long ret = 0;
		try {
			ipFile.seek(offset);
			ret |= (ipFile.readByte() & 0xFF);
			ret |= ((ipFile.readByte() << 8) & 0xFF00);
			ret |= ((ipFile.readByte() << 16) & 0xFF0000);
			ret |= ((ipFile.readByte() << 24) & 0xFF000000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * 从offset位置读取3个字节为一个long，因为java为big-endian格式，所以没办法
	 * 用了这么一个函数来做转换
	 * @param offset 整数的起始偏移
	 * @return 读取的long值，返回-1表示读取文件失败
	 * @throws IOException 
	 */
	private long readLong3(long offset) throws IOException {
		ipFile.seek(offset);
		return readLong3();
	}	
	
	/**
	 * 从当前位置读取3个字节转换成long
	 * @return 读取的long值，返回-1表示读取文件失败
	 */
	private long readLong3() {
		long ret = 0;
		try {
			ipFile.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}
  
	/**
	 * 从offset位置读取四个字节的ip地址放入ip数组中，读取后的ip为big-endian格式，但是
	 * 文件中是little-endian形式，将会进行转换
	 * @param offset
	 * @param ip
	 * @throws IOException 
	 */
	private void readIP(long offset, byte[] ip) throws IOException {
		ipFile.seek(offset);
		ipFile.readFully(ip);
		rotateIp(ip);
	}
	
	/**
	 * 从offset位置读取四个字节的ip地址放入ip数组中，读取后的ip为big-endian格式，但是
	 * 文件中是little-endian形式，将会进行转换
	 * @param offset
	 * @param ip
	 */
	private void readIP(int offset, byte[] ip) {
	    mbb.position(offset);
	    mbb.get(ip);
	    rotateIp(ip);
	}
	private void rotateIp(byte[] ip){
		byte temp = ip[0];
		ip[0] = ip[3];
		ip[3] = temp;
		temp = ip[1];
		ip[1] = ip[2];
		ip[2] = temp;
	}
	
	/**
	 * 把类成员ip和beginIp比较，注意这个beginIp是big-endian的
	 * @param ip 要查询的IP
	 * @param beginIp 和被查询IP相比较的IP
	 * @return 相等返回0，ip大于beginIp则返回1，小于返回-1。
	 */
	private int compareIP(byte[] ip, byte[] beginIp) {
		for(int i = 0; i < 4; i++) {
			int r = compareByte(ip[i], beginIp[i]);
			if(r != 0)
				return r;
		}
		return 0;
	}
	
	/**
	 * 把两个byte当作无符号数进行比较
	 * @param b1
	 * @param b2
	 * @return 若b1大于b2则返回1，相等返回0，小于返回-1
	 */
	private int compareByte(byte b1, byte b2) {
		if((b1 & 0xFF) > (b2 & 0xFF)) // 比较是否大于
			return 1;
		else if((b1 ^ b2) == 0)// 判断是否相等
			return 0;
		else 
			return -1;
	}
	
	/**
	 * 这个方法将根据ip的内容，定位到包含这个ip国家地区的记录处，返回一个绝对偏移
	 * 方法使用二分法查找。
	 * @param ip 要查询的IP
	 * @return 如果找到了，返回结束IP的偏移，如果没有找到，返回-1
	 * @throws IOException 
	 */
	private long locateIP(byte[] ip) throws IOException {
		long m = 0;
		int r;
		// 比较第一个ip项
		readIP(ipBegin, b4);
		r = compareIP(ip, b4);
		if(r == 0) return ipBegin;
		else if(r < 0) return -1;
		// 开始二分搜索
		for(long i = ipBegin, j = ipEnd; i < j; ) {
			m = getMiddleOffset(i, j);
			readIP(m, b4);
			r = compareIP(ip, b4);
			// log.debug(Utils.getIpStringFromBytes(b));
			if(r > 0)
				i = m;
			else if(r < 0) {
				if(m == j) {
					j -= IP_RECORD_LENGTH;
					m = j;
				} else 
					j = m;
			} else
				return readLong3(m + 4);
		}
		// 如果循环结束了，那么i和j必定是相等的，这个记录为最可能的记录，但是并非
		//     肯定就是，还要检查一下，如果是，就返回结束地址区的绝对偏移
		m = readLong3(m + 4);
		readIP(m, b4);
		r = compareIP(ip, b4);
		if(r <= 0) return m;
		else return -1;
	}
	
	/**
	 * 得到begin偏移和end偏移中间位置记录的偏移
	 * @param begin
	 * @param end
	 * @return
	 */
	private long getMiddleOffset(long begin, long end) {
		long records = (end - begin) / IP_RECORD_LENGTH;
		records >>= 1;
		if(records == 0) records = 1;
		return begin + records * IP_RECORD_LENGTH;
	}
	
	/**
	 * 给定一个ip国家地区记录的偏移，返回一个IPLocation结构
	 * @param offset 国家记录的起始偏移
	 * @return IPLocation对象
	 * @throws IOException 
	 */
	private IPLocation getIPLocation(long offset) throws IOException {
		IPLocation loc=new IPLocation();
		// 跳过4字节ip
		ipFile.seek(offset + 4);
		// 读取第一个字节判断是否标志字节
		byte b = ipFile.readByte();
		if(b == REDIRECT_MODE_1) {
			// 读取国家偏移
			long countryOffset = readLong3();
			// 跳转至偏移处
			ipFile.seek(countryOffset);
			// 再检查一次标志字节，因为这个时候这个地方仍然可能是个重定向
			b = ipFile.readByte();
			if(b == REDIRECT_MODE_2) {
				loc.setCountry (  readString(readLong3()));
				ipFile.seek(countryOffset + 4);
			} else
				loc.setCountry ( readString(countryOffset));
			// 读取地区标志
			loc.setArea( readArea(ipFile.getFilePointer()));
		} else if(b == REDIRECT_MODE_2) {
			loc.setCountry ( readString(readLong3()));
			loc.setArea( readArea(offset + 8));
		} else {
			loc.setCountry (  readString(ipFile.getFilePointer() - 1));
			loc.setArea( readArea(ipFile.getFilePointer()));
		}
		return loc;
	}	
	
	/**
	 * 给定一个ip国家地区记录的偏移，返回一个IPLocation结构，此方法应用与内存映射文件方式
	 * @param offset 国家记录的起始偏移
	 * @return IPLocation对象
	 * @throws IOException 
	 */
	private IPLocation getIPLocation(int offset) throws IOException {
		IPLocation loc=new IPLocation();
		// 跳过4字节ip
	    mbb.position(offset + 4);
		// 读取第一个字节判断是否标志字节
		byte b = mbb.get();
		if(b == REDIRECT_MODE_1) {
			// 读取国家偏移
			int countryOffset = readInt3();
			// 跳转至偏移处
			mbb.position(countryOffset);
			// 再检查一次标志字节，因为这个时候这个地方仍然可能是个重定向
			b = mbb.get();
			if(b == REDIRECT_MODE_2) {
				loc.setCountry (  readString(readInt3()));
				mbb.position(countryOffset + 4);
			} else
				loc.setCountry (  readString(countryOffset));
			// 读取地区标志
			loc.setArea(readArea(mbb.position()));
		} else if(b == REDIRECT_MODE_2) {
			loc.setCountry ( readString(readInt3()));
			loc.setArea(readArea(offset + 8));
		} else {
			loc.setCountry (  readString(mbb.position() - 1));
			loc.setArea(readArea(mbb.position()));
		}
		return loc;
	}
	
	/**
	 * 从offset偏移开始解析后面的字节，读出一个地区名
	 * @param offset 地区记录的起始偏移
	 * @return 地区名字符串
	 * @throws IOException
	 */
	private String readArea(long offset) throws IOException {
		ipFile.seek(offset);
		byte b = ipFile.readByte();
		if(b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
			long areaOffset = readLong3(offset + 1);
			if(areaOffset == 0)
				return unknown_area;
			else
				return readString(areaOffset);
		} else
			return readString(offset);
	}
	
	/**
	 * @param offset 地区记录的起始偏移
	 * @return 地区名字符串
	 * @throws IOException 
	 */
	private String readArea(int offset) throws IOException {
		mbb.position(offset);
		byte b = mbb.get();
		if(b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
			int areaOffset = readInt3();
			if(areaOffset == 0)
				return unknown_area;
			else
				return readString(areaOffset);
		} else
			return readString(offset);
	}
	
	/**
	 * 从offset偏移处读取一个以0结束的字符串
	 * @param offset 字符串起始偏移
	 * @return 读取的字符串，出错返回空字符串
	 * @throws IOException 
	 */
	private String readString(long offset) throws IOException {
		ipFile.seek(offset);
		bout.reset();
		for(byte b=ipFile.readByte(); b != 0; b = ipFile.readByte()){
			bout.write(b);
		}
		bout.flush();
		return byteArrayToString(bout.toByteArray());
	}
	
	/**
	 * 从内存映射文件的offset位置得到一个0结尾字符串
	 * @param offset 字符串起始偏移
	 * @return 读取的字符串，出错返回空字符串
	 * @throws IOException 
	 */
	private String readString(int offset) throws IOException {
		mbb.position(offset);
		bout.reset();
		for(byte b=mbb.get(); b!=0; b=mbb.get()){
			bout.write(b);
		}
		bout.flush();
		return byteArrayToString(bout.toByteArray());
	}
	private String byteArrayToString(byte[] buf) throws UnsupportedEncodingException{
		if(buf.length>0){
			return new String(buf,"GBK");
		}else{
			return "";
		}
	}
	public boolean isSrcChanged(){
		return ipSrc.lastModified()>this.ipSrcLastChanged;
	}
	public void close() throws IOException{
		IPSeeker.ipCache.cleanUp();
		this.ipFile.close();
	}
	public static IPSeeker getInstance() throws URISyntaxException, IOException{
		return getInstance((IPSeeker)null);
	}
	public static IPSeeker getInstance(IPSeeker earlier) throws URISyntaxException, IOException{
		return getInstance(earlier,ConfigManager.getInstance().getString(IPDAT_NAME,"ip.dat"));
	}
	public static IPSeeker getInstance(String srcName) throws URISyntaxException, IOException{
		return getInstance(null,srcName);
	}
	public static IPSeeker getInstance(File ipSrc) throws IOException{
		return getInstance(null,ipSrc);
	}
	public static IPSeeker getInstance(IPSeeker earlier,String srcName) throws URISyntaxException, IOException{
		return getInstance(earlier, new File(IPSeeker.class.getResource("/"+srcName).toURI()));
	}
	public static IPSeeker getInstance(IPSeeker earlier, File ipSrc) throws IOException{
		if(earlier==null){
			earlier=new IPSeeker(ipSrc);
		}else if(!earlier.ipSrc.equals(ipSrc) || earlier.isSrcChanged()){
			earlier.close();
			IPSeeker.ipCache.cleanUp();
			earlier=new IPSeeker(earlier.ipSrc);
		}
		return earlier;
	}
	
	/**
	 * 得到发起请求的ip
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request){
		String[] header=new String[]{"X-Forwarded-For","Proxy-Client-IP","WL-Proxy-Client-IP"};
		String ip=null;
		int i=0,l=header.length;
		do{
			ip=request.getHeader(header[i]);
		}while(++i<l && (Help.isEmpty(ip) || "unknown".equalsIgnoreCase(ip) || LOCALHOST.equals(ip)));
		if(Help.isEmpty(ip) || "unknown".equalsIgnoreCase(ip) || LOCALHOST.equals(ip)){
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 判断参数表示的ip是不是本机ip 127.0.0.1或localhost
	 * @param ip
	 * @return
	 */
	public static boolean isLocalhost(String ip){
		return "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip);
	}
}
