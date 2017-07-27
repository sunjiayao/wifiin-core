package com.wifiin.kv.protocol;

import java.util.UUID;

import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.kv.util.UUIDUtil;
import com.wifiin.util.message.IntMessageCodec;
import com.wifiin.util.message.Output;

/**
 * request: UUID_IN_16BYTES KEY_LEN KEY_UTF8 TYPE_BYTE  COMMAND_BYTE BODY_LEN BODY_BYTES DataType.valueOf(in.readUnsignedByte())
 * @author Running
 *
 */
public class RequestEncoder implements ProtocolRequestEncoder{
    
    @Override
    public void encode(Output output,String key,DataType dataType,Command command,byte[] body){
        UUID uuid=UUID.randomUUID();
        output.writeBytes(UUIDUtil.uuid2bytes(uuid));
        byte[] keyBytes=key.getBytes();
        IntMessageCodec.encode(keyBytes.length,output);
        output.writeBytes(keyBytes);
        output.writeByte(dataType.value());
        output.writeByte(command.value());
        IntMessageCodec.encode(body.length,output);
        output.writeBytes(body);
    }
}
