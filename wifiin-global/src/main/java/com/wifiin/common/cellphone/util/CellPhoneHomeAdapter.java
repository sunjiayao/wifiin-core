package com.wifiin.common.cellphone.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.wifiin.common.CommonConstant;
import com.wifiin.config.ConfigManager;
import com.wifiin.util.Help;


public class CellPhoneHomeAdapter implements CellPhoneHome{
    private static final Logger log=LoggerFactory.getLogger(CellPhoneHomeAdapter.class);
    /**
     * 用来决定cellphonehome的控制参数数据来源，分别有zookeeper和properties文件
     * 如果来源是zookeeper，则使用{@link com.wifiin.config.ConfigManager}获得配置参数。<br/>
     * 如果来源是properties文件，则使用{@link com.wifiin.common.CommonConstant}从classpath:current.constant.properties文件获取配置参数。<br/>
     * 配置参数的key是{@link com.wifiin.common.cellphone.util.CellPhoneHomeAdapter#FAILED_COUNT_TO_SHIFT}，类型是int。
     * 使用方法如下：<br/>
     * java -Dfailed.count.shift.src=CONSTANT_PROPERTIES,取值有CONSTANT_PROPERTIES和CONFIG_MANAGER，默认是CONSTANT_PROPERTIES
     */
    private static final String FAILED_COUNT_TO_SHIFT_SRC="failed.count.shift.src";
    /**
     * 当使用的手机号归属地查询实现类连续出异常次数达到这个key所指定的次数时，就跟它后面的那个实现交换位置。出错最多的实现最终将被交换到最后一个，最后面的实现不能主动与其它实现交换。
     */
    private static final String FAILED_COUNT_TO_SHIFT="failed.count.shift";
    /**
     * 默认的错误累计次数，默认是1
     */
    private static final int DEFAULT_FAILED_CONT_TO_SHIFT=1;
    private Map<CellPhoneHome,AtomicInteger> failedCellPhoneHomeQueryCountMap=new IdentityHashMap<>();
    private enum FailedCountShiftConfSrc{
        CONFIG_MANAGER {
            @Override
            public int failedCountToShift(){
                return ConfigManager.getInstance().getInt(FAILED_COUNT_TO_SHIFT,DEFAULT_FAILED_CONT_TO_SHIFT);
            }
        },
        CONSTANT_PROPERTIES {
            @Override
            public int failedCountToShift(){
                return CommonConstant.getIntConstant(FAILED_COUNT_TO_SHIFT,DEFAULT_FAILED_CONT_TO_SHIFT);
            }
        },
        DEFAULT{
            @Override
            public int failedCountToShift(){
                return CONSTANT_PROPERTIES.failedCountToShift();
            }
        };
        
        public abstract int failedCountToShift();
        public static int getFailedCountToShift(){
            return Help.convert(System.getProperty(FAILED_COUNT_TO_SHIFT_SRC),FailedCountShiftConfSrc.DEFAULT).failedCountToShift();
        }
    }
    @Override
    public String getURL(String phone){
        throw new IllegalAccessError("useless in adapter");
    }

    @Override
    public String[] parseResponse(String response) throws Exception{
        throw new IllegalAccessException("useless in adapter");
    }
    private int failedCountToShift(){
        return FailedCountShiftConfSrc.getFailedCountToShift();
    }
    private static final CellPhoneHomeAdapter instance=new CellPhoneHomeAdapter();
    public static CellPhoneHome getInstance(){
        return instance;
    }
    private static CellPhoneHome getLocalLibraryInstance(){
        getInstance();
        return instance.localLibrary;
    }
    public static String[] queryHomeByLocalLibrary(String phone){
        return getLocalLibraryInstance().query(phone);
    }
    public static String[] queryHome(String phone){
        return getInstance().query(phone);
    }
    private volatile List<CellPhoneHome> instances;
    private volatile CellPhoneHome localLibrary;
    private AtomicInteger idx=new AtomicInteger(0);
    private CellPhoneHomeAdapter(){
        localLibrary=new CellPhoneLocalLibrary();
        instances=ImmutableList.<CellPhoneHome>builder()
                .add(new CellPhone360(),
                     new CellPhoneBaiFuBao(),
                     new CellPhoneTaobao(),
                     new CellPhoneBaidu(),
                     new CellPhoneDaHanBank(),
                     new CellPhoneIteBlog(),
                     new CellPhoneTenPay())
                .build();
        instances.stream().forEach((h)->{
            failedCellPhoneHomeQueryCountMap.put(h,new AtomicInteger());
        });
    }
    private String[] query(String phone,boolean queryAll){
        String[] result=null;
        try{
            result = queryHomeByLocalLibrary(phone);
            if(queryAll){
                System.out.println(java.util.Arrays.toString(result));
            }else{
                return result;
            }
        }catch(Exception e){}
        int c=idx.getAndIncrement();
        for(int i=0,l=instances.size();i<l && (Help.isEmpty(result) || queryAll);i++){
            CellPhoneHome home=instances.get(Math.abs(c%l));
            c++;
            try{
                result=home.query(phone);
                resetFailedCount(home);
                if(queryAll){
                    System.out.println(java.util.Arrays.toString(result));
                }
            }catch(Exception e){
                log.warn("CellPhoneHome.query:"+home+';'+phone,e);
                shift(i,home);
            }
        }
        return result;
    }
    public String[] query(String phone){
        return query(phone,false);
    }
    private AtomicInteger failedCount(CellPhoneHome home){
        return failedCellPhoneHomeQueryCountMap.get(home);
    }
    private void resetFailedCount(CellPhoneHome home){
        failedCount(home).set(0);
    }
    private AtomicBoolean shifting=new AtomicBoolean(false);
    private void shift(int idx,CellPhoneHome home){
        AtomicInteger atomicCount=failedCount(home);
        if(idx<instances.size()-1 && atomicCount.incrementAndGet()>=this.failedCountToShift() && shifting.compareAndSet(false,true)){
            List list=Lists.newArrayList(instances);
            Collections.swap(list,idx,idx+1);
            synchronized(this){
                instances=list;
            }
            atomicCount.set(0);
            shifting.set(false);
        }
    }
    public static void main(String[] args){
//        ((CellPhoneHomeAdapter)CellPhoneHomeAdapter.getInstance()).query("13241886176",true);
//        ((CellPhoneHomeAdapter)CellPhoneHomeAdapter.getInstance()).idx.set(Integer.MAX_VALUE);
        ((CellPhoneHomeAdapter)CellPhoneHomeAdapter.getInstance()).query("13241886176");
    }
}
