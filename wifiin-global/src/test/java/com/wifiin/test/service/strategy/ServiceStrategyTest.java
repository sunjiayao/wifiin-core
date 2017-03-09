package com.wifiin.test.service.strategy;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.wifiin.loadbalance.Service;
import com.wifiin.loadbalance.ServiceCollection;
import com.wifiin.loadbalance.ServiceStatus;
import com.wifiin.loadbalance.Strategy;
import com.wifiin.loadbalance.strategy.MurmurHashTreeStrategy;
import com.wifiin.loadbalance.strategy.WeightRandomStrategy;
import com.wifiin.loadbalance.strategy.WeightRoundRobinStrategy;
import com.wifiin.util.Help;
import com.wifiin.util.WifiinUtil;

public class ServiceStrategyTest{
    public static class ServiceTest implements Service{
        private int i;
        private long w;
        public ServiceTest(int i){
            this.i=i;
            this.w=i;
        }
        public ServiceTest(int i,long weight){
            this.i=i;
            this.w=weight;
        }
        @Override
        public void notify(ServiceStatus status){
            // TODO Auto-generated method stub
            
        }

        @Override
        public long weight(){
            return w;
        }

        @Override
        public String name(){
            return Long.toString(i);
        }
        public String toString(){
            return name();
        }
    }
    private Service[] createService(int[] weights){
        Service[] ss=new Service[weights.length];
        for(int i=0,l=weights.length;i<l;i++){
            ss[i]=new ServiceTest(weights[i]);
        }
        return ss;
    }
    private Service[] createServiceSameWeight(int[] idx){
        Service[] ss=new Service[idx.length];
        for(int i=0,l=idx.length;i<l;i++){
            ss[i]=new ServiceTest(idx[i],1);
        }
        return ss;
    }
    private void testWeightStrategy(Service[] ss,int threads,Strategy strategy,boolean next) throws InterruptedException{
        long weightSum=0;
        for(int i=0,l=ss.length;i<l;i++){
            weightSum+=ss[i].weight();
        }
        ServiceCollection sc=new ServiceCollection(Arrays.asList(ss),strategy);
        Map<ServiceTest,Integer> freq=Maps.newConcurrentMap();
        ExecutorService es=Executors.newFixedThreadPool(threads);
        CountDownLatch latch=new CountDownLatch(threads);
        for(int i=0;i<threads;i++){
            es.submit(()->{
                for(int j=0;j<10000;j++){
                    ServiceTest st=(ServiceTest)(next?sc.next():sc.get(RandomStringUtils.random(10,"0123456789")));
//                    String name=st.name();
//                    System.out.println(name);
                    freq.compute(st,(k,c)->{
                        return Help.convert(c,0)+1;
                    });
                }
                latch.countDown();
            });
        }
        latch.await();
        System.out.println(freq);
        System.out.println("threads:"+threads);
        for(Map.Entry<ServiceTest,Integer> me:freq.entrySet()){
            long w=me.getKey().weight();
            System.out.println(w+"::"+((double)w/weightSum)+"    "+((double)me.getValue()/(10000*threads)));
        }
    }
    @Test
    public void testWeightRandomStrategy() throws InterruptedException{
        testWeightStrategy(createService(new int[]{20,30,25}),Runtime.getRuntime().availableProcessors(),new WeightRandomStrategy(),true);
    }
    @Test
    public void testWeightRoundRobinStrategy() throws InterruptedException{
        testWeightStrategy(createService(new int[]{20,30,25}),1,new WeightRoundRobinStrategy(),true);
    }
    @Test
    public void testMurmurHashStrategy() throws InterruptedException{
        testWeightStrategy(createService(new int[]{20,30,25}),1,new MurmurHashTreeStrategy(),false);
    }
    public static void main(String[] args) throws UnsupportedEncodingException{
        System.out.println(WifiinUtil.verify("45b33d9f72d8a3655e26251ba0f1829208a45112","20161207191730106","MGIwZDg4ZWuaJnE1OWE2NjdiN2ZhZTAwMmVmMWExMDg2NmU="));
    }
}
