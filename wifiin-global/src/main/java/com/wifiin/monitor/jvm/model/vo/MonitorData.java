package com.wifiin.monitor.jvm.model.vo;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.wifiin.log.LoggerFactory;
import com.wifiin.util.net.Localhost;
import com.wifiin.util.process.ProcessUtil;

public class MonitorData implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=-9075866810399718060L;
    private static final Logger log=LoggerFactory.getLogger(MonitorData.class);
    private static final String[] IPS;
    static{
        String[] tmp=null;
        try{
            java.util.Enumeration<NetworkInterface> nie=NetworkInterface.getNetworkInterfaces();
            List<String> ips=Lists.newArrayList();
            while(nie.hasMoreElements()){
                NetworkInterface ni=nie.nextElement();
                if(!ni.isLoopback()){
                    java.util.Enumeration<InetAddress> iae=ni.getInetAddresses();
                    while(iae.hasMoreElements()){
                        InetAddress ia=iae.nextElement();
                        if(!ia.isLoopbackAddress()){
                            ips.add(ia.getHostAddress());
                        }
                    }
                }
            }
            tmp=ips.toArray(new String[0]);
        }catch(Exception e){
            log.warn("Metrics:IP:",e);
        }
        IPS=tmp;
    }
    private String[] ips=IPS;
    private String mac=Localhost.getLocalMacInString();
    private int pid=ProcessUtil.getPid();
    private Map<String,Object> metrics;
    public String[] getIps(){
        return ips;
    }
    public String getMac(){
        return mac;
    }
    public int getPid(){
        return pid;
    }
    public Map<String,Object> getMetrics(){
        return metrics;
    }
    public void setMetrics(Map<String,Object> metrics){
        this.metrics=metrics;
    }
}
