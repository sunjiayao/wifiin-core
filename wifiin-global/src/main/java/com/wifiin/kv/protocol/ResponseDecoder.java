package com.wifiin.kv.protocol;

import com.wifiin.util.message.Input;
import com.wifiin.util.message.IntMessageCodec;

/**
 * UUID_IN_16BYTES STATUS CONTENT
 * @author Running
 *
 */
public class ResponseDecoder {
    public static void decodeUUID(ResponseMessage message,Input in){
        message.setUuid(in.read(16));
    }
    public static void decodeStatus(ResponseMessage message,Input in){
        message.setStatus(in.readUnsignedByte());
    }
    public static void decodeLength(ResponseMessage message,Input in){
        message.setLength((int)IntMessageCodec.decode(in));
    }
    public static void decodeBody(ResponseMessage message,Input in){
        message.setBody(in.read(message.getLength()));
    }
}
