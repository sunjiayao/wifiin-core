package com.wifiin.data;

public enum EncryptionType {
    NO_ENCRYPTION(0),AES(1),RSA_PUBLIC(2),RSA_PRIVATE(3);
    
    private int value;
    private EncryptionType(int value){
        this.value=value;
    }
    public int getValue(){
        return value;
    }
    public static EncryptionType eval(int value){
        EncryptionType[] cts=EncryptionType.values();
        for(int i=0,l=cts.length;i<l;i++){
            EncryptionType ct=cts[i];
            if(ct.value==value){
                return ct;
            }
        }
        return null;
    }
}
