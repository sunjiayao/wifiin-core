package com.wifiin.kv.protocol;

public class ResponseMessage implements Message{
    private byte[] uuid;
    private int status;
    private int length;
    private byte[] body;
    public ResponseMessage(){}
    public byte[] getUuid(){
        return uuid;
    }
    public void setUuid(byte[] uuid){
        this.uuid=uuid;
    }
    public int getStatus(){
        return status;
    }
    public void setStatus(int status){
        this.status=status;
    }
    public int getLength(){
        return length;
    }
    public void setLength(int length){
        this.length=length;
    }
    public byte[] getBody(){
        return body;
    }
    public void setBody(byte[] body){
        this.body=body;
    }
}
