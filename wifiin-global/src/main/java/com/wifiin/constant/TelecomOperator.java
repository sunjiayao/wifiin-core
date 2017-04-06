package com.wifiin.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
/**
 * 电信运营商
 * @author Running
 */
public enum TelecomOperator{
    SATELITE(-3,"卫星"),
    WIRELESS_NETWORK_CARD(-2,"上网卡"),
    VIRTUAL(-1,"虚拟"),
    CMCC(ApType.CMCC.getApId(),"移动"),
    CHINA_UNICOM(ApType.CHINA_UNICOM.getApId(),"联通"),
    CHINA_NET(ApType.CHINA_NET.getApId(),"电信");
    
    private int value;
    private String cnname;
    private TelecomOperator(int value,String cnname){
        this.value=value;
        this.cnname=cnname;
    }
    public int getValue(){
        return value;
    }
    /**
     * 构建telecomOperatorNameMap
     */
    private static Map<String,TelecomOperator> telecomOperatorNameMap;
    private static Map<String,TelecomOperator> getTelecomOperatorNameMap(){
        if(telecomOperatorNameMap==null){
            synchronized(TelecomOperator.class){
                if(telecomOperatorNameMap==null){
                    telecomOperatorNameMap=new ConcurrentHashMap<>();
                }
            }
        }
        return telecomOperatorNameMap;
    }
    /**
     * 得到指定中文名的运行商
     * @param cnname
     * @return
     */
    public static TelecomOperator getByCnName(String cnname){
        TelecomOperator operator=getTelecomOperatorNameMap().get(cnname);
        if(operator==null){
            return telecomOperatorNameMap.computeIfAbsent(cnname,(n)->{
                TelecomOperator[] ops=TelecomOperator.values();
                for(int i=0,l=ops.length;i<l;i++){
                    TelecomOperator op=ops[i];
                    if(n.indexOf(op.cnname)>=0){
                        return op;
                    }
                }
                return null;
             });
        }
        return operator;
    }
    /**
     * 构建telecomOperatorValueMap
     */
    private static Map<Integer,TelecomOperator> telecomOperatorValueMap;
    private static Map<Integer,TelecomOperator> getTelecomOperatorValueMap(){
        if(telecomOperatorValueMap==null){
            synchronized(TelecomOperator.class){
                if(telecomOperatorValueMap==null){
                    telecomOperatorValueMap=new ConcurrentHashMap<>();
                }
            }
        }
        return telecomOperatorValueMap;
    }
    /**
     * 得到指定值的运行商
     * @param value
     * @return
     */
    public static TelecomOperator getByValue(int value){
        TelecomOperator operator=getTelecomOperatorValueMap().get(value);
        if(operator==null){
            return telecomOperatorValueMap.computeIfAbsent(value,(v)->{
                TelecomOperator[] ops=TelecomOperator.values();
                for(int i=0,l=ops.length;i<l;i++){
                    TelecomOperator op=ops[i];
                    if(op.value==value){
                        return op;
                    }
                }
                return null;
            });
        }
        return operator;
    }
    /**
     * 移动号段正则
     */
    private static final Pattern CELL_PHONE=Pattern.compile("^1([38][0-9]|4[579]|5[0-35-9]|7[0135-8]|8\\d)\\d{8}$");
    private static final Pattern CMCC_SECTION=Pattern.compile("^1((34[0-8]|705)[0-9]{7}|(3[5-9]|47|5[012789]|78|8[23478])[0-9]{8})$");
    private static final Pattern CHINA_UNICOM_SECTION=Pattern.compile("^1((3[012]|[458]5|[578]6)[0-9]{8}|7(0[789]|1[89])[0-9]{7})$");
    private static final Pattern CHINA_NET_SECTION=Pattern.compile("^1(([35]3|77|8[019]|49)[0-9]{8}|(349|70[01])[0-9]{7})$");
    private static final Pattern VIRTUAL_SECTION=Pattern.compile("^1(7[01])[0-9]{8}$");
    private static final Pattern WIRELESS_NETWORK_CARD_SECTION=Pattern.compile("^14[0-9]{9}$");
    private static final Pattern SATELITE_SECTION=Pattern.compile("^1349[0-9]{7}$");
    public static boolean isCellPhone(String phone){
        return CELL_PHONE.matcher(phone).matches();
    }
    public static TelecomOperator getByPhone(String phone){
        if(CMCC_SECTION.matcher(phone).matches()){
            return CMCC;
        }else if(CHINA_UNICOM_SECTION.matcher(phone).matches()){
            return CHINA_UNICOM;
        }else if(CHINA_NET_SECTION.matcher(phone).matches()){
            return CHINA_NET;
        }
        return null;
    }
    private static boolean matches(Pattern regex,String phone){
        return regex.matcher(phone).matches();
    }
    public static boolean isVirtual(String phone){
        return matches(VIRTUAL_SECTION,phone);
    }
    public static boolean isWirelessNetworkCard(String phone){
        return matches(WIRELESS_NETWORK_CARD_SECTION,phone);
    }
    public static boolean isSatelite(String phone){
        return matches(SATELITE_SECTION,phone);
    }
    private static boolean matches(TelecomOperator expected,TelecomOperator actually){
        return expected.equals(actually);
    }
    public static boolean isCMCCPhone(String phone){
        return matches(CMCC,getByPhone(phone));
    }
    public static boolean isChinaUnicomPhone(String phone){
        return matches(CHINA_UNICOM,getByPhone(phone));
    }
    public static boolean isChinaNetPhone(String phone){
        return matches(CHINA_NET,getByPhone(phone));
    }
    public static boolean isCMCCIMSI(String imsi){
        return matches(CMCC,getByImsi(imsi));
    }
    public static boolean isChinaUnicomIMSI(String imsi){
        return matches(CHINA_UNICOM,getByImsi(imsi));
    }
    public static boolean isChinaNetIMSI(String imsi){
        return matches(CHINA_NET,getByImsi(imsi));
    }
    private static final Pattern CMCC_IMSI_START=Pattern.compile("^4600[027]\\d*$");
    private static final Pattern CHINA_UNICOM_START=Pattern.compile("^46001\\d*$");
    private static final Pattern CHINA_NET_START=Pattern.compile("^46003\\d*$");
    public static TelecomOperator getByImsi(String imsi){
        if(CMCC_IMSI_START.matcher(imsi).matches()){
            return CMCC;
        }else if(CHINA_UNICOM_START.matcher(imsi).matches()){
            return CHINA_UNICOM;
        }else if(CHINA_NET_START.matcher(imsi).matches()){
            return CHINA_NET;
        }else{
            return null;
        }
    }
}
