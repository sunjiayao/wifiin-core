package com.wifiin.data;

public enum Bytes2StringType {
    BINARY(0),BASE64(1),BASE64_BYTES(2);
    
    private int value;
    private Bytes2StringType(int value){
        this.value=value;
    }
    public int getValue(){
        return value;
    }
    public static Bytes2StringType eval(int value){
        Bytes2StringType[] cts=Bytes2StringType.values();
        for(int i=0,l=cts.length;i<l;i++){
            Bytes2StringType ct=cts[i];
            if(ct.value==value){
                return ct;
            }
        }
        return null;
    }
}
