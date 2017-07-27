package com.wifiin.kv.protocol;

import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.kv.Result;

public class RequestMessage implements Message{
    private byte[] uuid;
    private int keyLength;
    private byte[] key;
    private DataType type;
    private Command<Result> cmd;
    private int bodyLength;
    private byte[] body;
    
    public RequestMessage(){}

    public byte[] getUuid(){
        return uuid;
    }

    public void setUuid(byte[] uuid){
        this.uuid=uuid;
    }

    public int getKeyLength(){
        return keyLength;
    }

    public void setKeyLength(int keyLength){
        this.keyLength=keyLength;
    }

    public byte[] getKey(){
        return key;
    }

    public void setKey(byte[] key){
        this.key=key;
    }

    public DataType getType(){
        return type;
    }

    public void setType(DataType type){
        this.type=type;
    }

    public Command<Result> getCmd(){
        return cmd;
    }

    public void setCmd(Command<Result> cmd){
        this.cmd=cmd;
    }

    public int getBodyLength(){
        return bodyLength;
    }

    public void setBodyLength(int bodyLength){
        this.bodyLength=bodyLength;
    }

    public byte[] getBody(){
        return body;
    }

    public void setBody(byte[] body){
        this.body=body;
    }
    
}
