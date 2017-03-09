package com.wifiin.data;

public enum SerializationType {
    JSON(1),JSON_BYTES(2);
    private int value;
    private SerializationType(int value){
        this.value=value;
    }
    public int getValue(){
        return value;
    }
    public static SerializationType eval(int value){
        SerializationType[] cts=SerializationType.values();
        for(int i=0,l=cts.length;i<l;i++){
            SerializationType ct=cts[i];
            if(ct.value==value){
                return ct;
            }
        }
        return null;
    }
}
