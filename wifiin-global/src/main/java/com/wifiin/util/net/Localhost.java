package com.wifiin.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.wifiin.exception.LocalhostException;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;
/**
 * 本机网络数据
 * @author Running
 *
 */
public class Localhost {
	private static byte[] localIpBytes;
	private static long localIpLong;
	private static String localIpHex;
	private static String localIpStr;
	private static String localIpStrNoDot;
	private static byte[] localMacBytes;
	private static long localMacLong;
	private static String localMacStr;
	private static String localHostName;
	/**
	 * 本机网络数据
	 * @return
	 */
	public static InetAddress getLocalhost(){
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new LocalhostException(e);
		}
	}
	/**
	 * 本机ip
	 * @return
	 */
	public static byte[] getLocalIpInBytes(){
		if(Help.isEmpty(localIpBytes)){
			synchronized(Localhost.class){
				if(Help.isEmpty(localIpBytes)){
					localIpBytes=getLocalIpInBytes0();
				}
			}
		}
		return localIpBytes;
	}
	/**
	 * 本机ip
	 * @return
	 */
	private static byte[] getLocalIpInBytes0(){
	    return getLocalhost().getAddress();
	}
	/**
	 * 本机ip，十六进制
	 * @return
	 */
	public static String getLocalIpInHex(){
	    if(Help.isEmpty(localIpHex)){
	        synchronized(Localhost.class){
	            if(Help.isEmpty(localIpHex)){
	                localIpHex=getLocalIpInHex0();
	            }
	        }
	    }
	    return localIpHex;
	}
	/**
	 * 本机ip，十六进制
	 * @return
	 */
	private static String getLocalIpInHex0(){
	    localIpBytes=getLocalIpInBytes0();
	    StringBuilder hex=ThreadLocalStringBuilder.builder();
	    int mask=0xff;
	    for(int i=0,l=localIpBytes.length;i<l;i++){
	        String part=Integer.toHexString(localIpBytes[i]&mask);
	        if(part.length()==1){
	            hex.append("0");
	        }
	        hex.append(part);
	    }
	    return hex.toString();
	}
	/**
	 * 本机ip
	 * @return
	 */
	public static long getLocalIpInLong(){
	    if(localIpLong==0){
	        synchronized(Localhost.class){
	            if(localIpLong==0){
	                localIpLong=getLocalIpInLong0();
	            }
	        }
	    }
	    return localIpLong;
	}
	/**
	 * 本机Ip
	 * @return
	 */
	private static long getLocalIpInLong0(){
	    return Long.parseLong(localIpStrNoDot=getLocalIpInStringNoDot0());
	}
	/**
	 * 本机ip，不带点
	 * @return
	 */
	public static String getLocalIpInStringNoDot(){
	    if(localIpStrNoDot==null){
            synchronized(Localhost.class){
                if(localIpStrNoDot==null){
                    localIpStrNoDot=getLocalIpInStringNoDot0();
                }
            }
        }
        return localIpStrNoDot;
	}
	/**
	 * 本机ip，不带点
	 * @return
	 */
	public static String getLocalIpInStringNoDot0(){
	    byte[] ipBytes=getLocalIpInBytes();
        int mask=0xff;
        StringBuilder ipBuilder=ThreadLocalStringBuilder.builder();
        for(int i=0;i<4;i++){
            String part=Integer.toString(ipBytes[i]&mask);
            String zeros=Help.concat("0",3-part.length());
            ipBuilder.append(zeros).append(part);
        }
        return ipBuilder.toString();
	}
	/**
	 * 本机ip，带点
	 * @return
	 */
	public static String getLocalIpInString(){
		if(Help.isEmpty(localIpStr)){
			synchronized(Localhost.class){
				if(Help.isEmpty(localIpStr)){
					localIpStr=getLocalIpInString0();
				}
			}
		}
		return localIpStr;
	}
	/**
	 * 本机ip，带点
	 * @return
	 */
	private static String getLocalIpInString0(){
	    return getLocalhost().getHostAddress();
	}
	/**
	 * 本机网络接口
	 * @return
	 */
	public static NetworkInterface getNetworkInterface(){
		try {
			return NetworkInterface.getByInetAddress(getLocalhost());
		} catch (SocketException e) {
			throw new LocalhostException(e);
		}
	}
	/**
	 * 本机mac
	 * @return
	 */
	public static byte[] getLocalMacInBytes(){
		if(Help.isEmpty(localMacBytes)){
			synchronized(Localhost.class){
				if(Help.isEmpty(localMacBytes)){
				    localMacBytes=getLocalMacInBytes0();
				}
			}
		}
		return localMacBytes;
	}
	/**
	 * 本机mac
	 * @return
	 */
	private static byte[] getLocalMacInBytes0(){
	    try {
            return getNetworkInterface().getHardwareAddress();
        } catch (SocketException e) {
            throw new LocalhostException(e);
        }
	}
	/**
	 * 本机mac
	 * @return
	 */
	public static long getLocalMacLong(){
		if(localMacLong==0){
			synchronized(Localhost.class){
				if(localMacLong==0){
					localMacLong=getLocalMacLong0();
				}
			}
		}
		return localMacLong;
	}
	/**
	 * 本机mac
	 * @return
	 */
	private static long getLocalMacLong0(){
	    long mac=0;
        localMacBytes=getLocalMacInBytes0();
        for(int i=0,l=localMacBytes.length;i<l;i++){
            mac=(mac<<8)|localMacBytes[i];
        }
        return mac;
	}
	/**
	 * 本机mac
	 * @return
	 */
	public static String getLocalMacInString(){
		if(Help.isEmpty(localMacStr)){
			synchronized(Localhost.class){
				if(Help.isEmpty(localMacStr)){
					localMacStr=getLocalMacInString0();
				}
			}
		}
		return localMacStr;
	}
	/**
	 * 本机mac
	 * @return
	 */
	private static String getLocalMacInString0(){
	    return Long.toString(localMacLong=getLocalMacLong0(),16);
	}
	/**
	 * 本机主机名
	 * @return
	 */
	public static String getLocalHostName(){
		if(Help.isEmpty(localHostName)){
			synchronized(Localhost.class){
				if(Help.isEmpty(localHostName)){
					localHostName=getLocalHostName0();
				}
			}
		}
		return localHostName;
	}
	/**
	 * 本机主机名
	 * @return
	 */
	public static String getLocalHostName0(){
	    return getLocalhost().getHostName();
	}
	public static void main(String[] args) throws IOException {
	    java.util.Enumeration<NetworkInterface> nie=NetworkInterface.getNetworkInterfaces();
	    while(nie.hasMoreElements()){
	        NetworkInterface ni=nie.nextElement();
	        System.out.println("################");
	        System.out.println("loopback:"+ni.isLoopback()+";up:"+ni.isUp()+";virtual:"+ni.isVirtual());
	        java.util.Enumeration<InetAddress> iae=ni.getInetAddresses();
	        while(iae.hasMoreElements()){
	            InetAddress ia=iae.nextElement();
	            System.out.println("@@@@@@@@@@@ip:"+ia.getHostAddress()+";siteLocal:"+ia.isSiteLocalAddress()+";anyLocal:"+ia.isAnyLocalAddress()+";linkLocal:"+ia.isLinkLocalAddress());
	        }
	        System.out.println("################");
	    }
	}
}
