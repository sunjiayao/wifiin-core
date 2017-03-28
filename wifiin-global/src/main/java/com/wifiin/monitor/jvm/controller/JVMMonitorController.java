package com.wifiin.monitor.jvm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wifiin.monitor.jvm.JVMMonitor;
import com.wifiin.monitor.jvm.impl.JVMMonitorImpl;
import com.wifiin.monitor.jvm.model.vo.MonitorData;

@Controller
public class JVMMonitorController implements JVMMonitor{
    private JVMMonitor jvmMonitor=new JVMMonitorImpl();
    @RequestMapping("/health/jvm")
    @Override
    public @ResponseBody MonitorData monitor(){
        return jvmMonitor.monitor();
    }
    
}
