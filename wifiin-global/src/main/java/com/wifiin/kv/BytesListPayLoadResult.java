package com.wifiin.kv;

import java.util.List;
import java.util.UUID;

import com.wifiin.kv.util.UUIDUtil;

public class BytesListPayLoadResult extends Result{
    /**
     * 
     */
    private static final long serialVersionUID=-5746341817820699921L;
    public byte[] uuid;
    private List<byte[]> payload;
    public BytesListPayLoadResult(Result status,byte[] uuid,List<byte[]> payload){
        super(status.status());
        this.uuid=uuid;
        this.payload=payload;
    }
    public byte[] uuidBytes(){
        return uuid;
    }
    public UUID uuid(){
        return UUIDUtil.parse(uuid);
    }
    public List<byte[]> payload(){
        return payload;
    }
}
