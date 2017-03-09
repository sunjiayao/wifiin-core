package com.wifiin.data;

public enum CompressType {
    NO_COMPRESS(0),GZIP(1);
    private int value;
    private CompressType(int value){
        this.value=value;
    }
    public int getValue(){
        return value;
    }
    public static CompressType eval(int value){
        CompressType[] cts=CompressType.values();
        for(int i=0,l=cts.length;i<l;i++){
            CompressType ct=cts[i];
            if(ct.value==value){
                return ct;
            }
        }
        return null;
    }
}
