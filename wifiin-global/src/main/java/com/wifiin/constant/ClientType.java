package com.wifiin.constant;

/**
 * 客户端类型，对应webservice接口的<os></os>节点和数据库的os字段
 * 
 * @author running
 *         
 */
public enum ClientType {
    COMMON(-1), ANDROID(0), IOS(3), WP(2), IOS_SHOP(3);
    private int type;
    
    private ClientType(int type) {
        this.type = type;
    }
    
    public int getType(){
        return type;
    }
    
    public static String getName(int type){
        ClientType[] cts = ClientType.values();
        for (int i = 0, l = cts.length; i < l; i++) {
            ClientType ct = cts[i];
            if (ct.getType() == type) {
                return ct.name();
            }
        }
        return null;
    }
    
    public static boolean contains(int type){
        ClientType[] cts = ClientType.values();
        for (int i = 0, l = cts.length; i < l; i++) {
            ClientType ct = cts[i];
            if (ct.getType() == type) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean valid(int type){
        return contains(type);
    }
    
    public static boolean isAndroid(int os){
        return os == ANDROID.getType();
    }
    
    public static boolean isWp(int os){
        return os == WP.getType();
    }
    
    public static boolean isIosShop(int os){
        return os == IOS_SHOP.getType();
    }
    
    public static boolean isIOS(int os){
        return os == IOS.getType();
    }
    public static int getType(String osName){
        return getOs(osName).type;
    }
    public static ClientType getOs(String osName){
        try{
            return ClientType.valueOf(osName.toUpperCase());
        }catch(IllegalArgumentException e){
            throw null;
        }
    }
}