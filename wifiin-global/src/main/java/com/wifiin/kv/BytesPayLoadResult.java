package com.wifiin.kv;

import java.util.UUID;

import com.wifiin.kv.util.UUIDUtil;

public class BytesPayLoadResult extends Result{
    /**
     * 
     */
    private static final long serialVersionUID=3766377939769736873L;
    private byte[] uuid;
    private byte[] payload;
    private BytesPayLoadResult(Result status,byte[] uuid){
        super(status.status());
        this.uuid=uuid;
    }
    public BytesPayLoadResult(Result status,byte[] uuid,byte[] payload){
        this(status,uuid);
        this.payload=payload;
    }
    public BytesPayLoadResult(Result status,byte[] uuid,byte[] payload,int start,int end){
        this(status,uuid);
        byte[] result=new byte[end-start];
        System.arraycopy(payload,start,result,0,result.length);
        this.payload=result;
    }
    public byte[] uuidBytes(){
        return uuid;
    }
    public UUID uuid(){
        return UUIDUtil.parse(uuid);
    }
    public byte[] payload(){
        return payload;
    }
}
