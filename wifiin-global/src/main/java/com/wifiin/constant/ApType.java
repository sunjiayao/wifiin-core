package com.wifiin.constant;

/**
 * 账号类型 对应BdApAccounter.wifiinApId
 * 
 * @author running
 *         
 */
public enum ApType {
    CMCC(1, "CMCC", "CM"), 
    CHINA_UNICOM(2, "ChinaUnicom", "CU"), 
    CHINA_NET(3, "ChinaNet", "CN"), 
    CMCC_EDU(4, "cmcc-edu", "CME"), 
    CHINA_UNICOM_EDU(5, "chinaunicom-edu", "CUE"), 
    CHINA_NET_EDU(6, "chinanet-edu", "CNE"), 
    CMCC_WEB(7,"CMCC-WEB", "CMW"), 
    PRIVATE_WIFI(1000000, "PRIVATE_WIFI", ""), 
    WOWIFI(2000001, "wowifi","沃云"), 
    MSH100(2000002, "100msh", "百米"), 
    SOSOWIFI(2000003, "sosowifi", "飕飕"), 
    ICITY(2000004,"icity", "华思"), 
    BOING(2100001, "boing", "boing"), 
    IPASS(2100002, "ipass", "ipass");
    private int apid;
    private String name;
    private String shortcut;
    
    private ApType(int apid, String name, String shortcut) {
        this.apid = apid;
        this.name = name;
        this.shortcut = shortcut;
    }
    
    public int getApId(){
        return apid;
    }
    
    public String getName(){
        return name;
    }
    
    public String getShortcut(){
        return this.shortcut;
    }
    
    public static String getName(int apid){
        ApType[] ats = ApType.values();
        for (int i = 0, l = ats.length; i < l; i++) {
            ApType at = ats[i];
            if (at.apid == apid) {
                return at.name;
            }
        }
        return null;
    }
    
    public static String getShortcut(int apid){
        ApType[] ats = ApType.values();
        for (int i = 0, l = ats.length; i < l; i++) {
            ApType at = ats[i];
            if (at.apid == apid) {
                return at.shortcut;
            }
        }
        return null;
    }
    
    public static int getApId(String name){
        ApType[] ats = ApType.values();
        for (int i = 0, l = ats.length; i < l; i++) {
            ApType at = ats[i];
            if (at.name.equalsIgnoreCase(name)) {
                return at.apid;
            }
        }
        return 0;
    }
    
    public static boolean valid(int apid){
        ApType[] ats = ApType.values();
        for (int i = 0, l = ats.length; i < l; i++) {
            ApType at = ats[i];
            if (at.apid == apid) {
                return true;
            }
        }
        return false;
    }
}