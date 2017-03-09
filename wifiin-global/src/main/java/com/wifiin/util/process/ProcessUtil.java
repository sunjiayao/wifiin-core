package com.wifiin.util.process;

import java.lang.management.ManagementFactory;

import com.wifiin.util.Help;

public class ProcessUtil {
    private static int pid;
    private static String pidHex;
    /**
     * 得到JAVA进程号
     */
    public static int getPid(){
        if(pid==0){
            synchronized(ProcessUtil.class){
                if(pid==0){
                    pid=getPid0();
                }
            }
        }
    	return pid;
    }
    /**
     * 得到进程号
     * @return
     */
    private static int getPid0(){
        String pidKey="java.pid";
        String pid=System.getProperty(pidKey);
        if(Help.isEmpty(pid)){
            String pName=ManagementFactory.getRuntimeMXBean().getName();
            pid=pName.substring(0,pName.indexOf('@'));
        }
        return Integer.parseInt(pid);
    }
    /**
     * 得到十六进制进程号
     * @return
     */
    public static String getPidHex(){
        if(pidHex==null){
            synchronized(ProcessUtil.class){
                if(pidHex==null){
                    pidHex=getPidHex0();
                }
            }
        }
        return pidHex;
    }
    /**
     * 得到十六进制进程号
     * @return
     */
    private static String getPidHex0(){
        return Integer.toHexString(pid=getPid0());
    }
}
