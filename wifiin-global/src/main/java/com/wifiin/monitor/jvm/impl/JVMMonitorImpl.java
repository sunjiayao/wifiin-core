package com.wifiin.monitor.jvm.impl;

import java.lang.management.ManagementFactory;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.common.collect.Maps;
import com.wifiin.monitor.jvm.JVMMonitor;
import com.wifiin.monitor.jvm.model.vo.MonitorData;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class JVMMonitorImpl implements JVMMonitor{
    private MetricRegistry registry=new MetricRegistry();
    public JVMMonitorImpl(){
//        registerAll("buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()), registry);
        registerAll("classloading",new ClassLoadingGaugeSet(),registry);
        register("file.descriptor.ratio",new FileDescriptorRatioGauge(),registry);
        registerAll("gc", new GarbageCollectorMetricSet(), registry);
        registerAll("memory", new MemoryUsageGaugeSet(), registry);
        registerAll("threads", new ThreadStatesGaugeSet(), registry);
        registerAll("jvm.attr",new JvmAttributeGaugeSet(),registry);
    }
    @SuppressWarnings("rawtypes")
    @Override
    public MonitorData monitor(){
        MonitorData data=new MonitorData();
        Map<String,Object> values=Maps.newTreeMap();
        for(Map.Entry<String,Gauge> entry:registry.getGauges().entrySet()){
            values.put(entry.getKey(),entry.getValue().getValue());
        }
        data.setMetrics(values);
        return data;
    }
    
    private void registerAll(String prefix, MetricSet metricSet, MetricRegistry registry) {
        for (Map.Entry<String, Metric> entry : metricSet.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(ThreadLocalStringBuilder.builder().append(prefix).append(".").append(entry.getKey()).toString(), 
                        (MetricSet) entry.getValue(), registry);
            } else {
                registry.register(ThreadLocalStringBuilder.builder().append(prefix).append(".").append(entry.getKey()).toString(), 
                        entry.getValue());
            }
        }
    }
    private void register(String key,Metric metric,MetricRegistry registry){
        registry.register(key,metric);
    }
    public static void main(String[] args) throws InterruptedException{
        JVMMonitor monitor=new JVMMonitorImpl();
        for(;;){
            Thread.sleep(1000);
            MonitorData data=monitor.monitor();
            Map<String,Object> metrics=data.getMetrics();
            for(Map.Entry<String,Object> entry:metrics.entrySet()){
                System.out.println(entry);
            }
            System.out.println("#####################");
        }
    }
}
