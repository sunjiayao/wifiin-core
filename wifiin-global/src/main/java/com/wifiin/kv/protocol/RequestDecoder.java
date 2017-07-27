package com.wifiin.kv.protocol;

import com.wifiin.kv.DataType;
import com.wifiin.util.message.Input;
import com.wifiin.util.message.IntMessageCodec;

/**
 * request: UUID_IN_16BYTES KEY_LEN KEY_UTF8 TYPE_BYTE  COMMAND_BYTE BODY_LEN BODY_BYTES 
 * @author Running
 *
 */
public class RequestDecoder{
    public static void decodeUUID(RequestMessage message,Input in){
        message.setUuid(in.read(16));
    }
    public static void decodeKeyLength(RequestMessage message,Input in){
        message.setKeyLength((int)IntMessageCodec.decode(in));
    }
    public static void decodeKey(RequestMessage message,Input in){
        message.setKey(in.read(message.getKeyLength()));
    }
    public static void decodeDataType(RequestMessage message,Input in){
        message.setType(DataType.valueOf(in.readUnsignedByte()));
    }
    public static void decodeCmd(RequestMessage message,Input in){
        message.setCmd(message.getType().command(in.readUnsignedByte()));
    }
    public static void decodeBodyLength(RequestMessage message,Input in){
        message.setBodyLength((int)IntMessageCodec.decode(in));
    }
    public static void decodeBody(RequestMessage message,Input in){
        message.setBody(in.read(message.getBodyLength()));
    }
}
