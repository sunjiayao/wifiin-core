package com.wifiin.kv;

import java.util.List;

public class BytesListPayLoadResult extends Result{
    /**
     * 
     */
    private static final long serialVersionUID=-5746341817820699921L;
    private List<byte[]> payload;
    public BytesListPayLoadResult(Result status,List<byte[]> payload){
        super(status.status());
        this.payload=payload;
    }
    public List<byte[]> payload(){
        return payload;
    }
}
