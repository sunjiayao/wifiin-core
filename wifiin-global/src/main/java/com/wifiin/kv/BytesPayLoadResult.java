package com.wifiin.kv;

public class BytesPayLoadResult extends Result{
    /**
     * 
     */
    private static final long serialVersionUID=3766377939769736873L;
    private byte[] payload;
    public BytesPayLoadResult(Result status,byte[] payload){
        super(status.status());
        this.payload=payload;
    }
    public BytesPayLoadResult(Result status,byte[] payload,int start,int end){
        super(status.status());
        byte[] result=new byte[end-start];
        System.arraycopy(payload,start,result,0,result.length);
        this.payload=result;
    }
    public byte[] payload(){
        return payload;
    }
}
