package com.wifiin.kv.protocol;

import java.util.UUID;

import com.wifiin.kv.util.UUIDUtil;
import com.wifiin.util.Help;
import com.wifiin.util.message.IntMessageCodec;
import com.wifiin.util.message.Output;

/**
 * UUID_IN_16BYTES STATUS CONTENT
 * @author Running
 *
 */
public class ResponseEncoder implements ProtocolResponseEncoder{
    public static final ResponseEncoder instance=new ResponseEncoder();
    @Override
    public void encode(Output output,UUID uuid,int status,byte[] content){
        encode(output,UUIDUtil.uuid2bytes(uuid),status,content);
    }

    @Override
    public void encode(Output output,byte[] uuid,int status,byte[] content){
        output.writeBytes(uuid);
        output.writeByte(status);
        if(Help.isNotEmpty(content)){
            IntMessageCodec.encode(content.length,output);
            output.writeBytes(content);
        }
    }
    
}
