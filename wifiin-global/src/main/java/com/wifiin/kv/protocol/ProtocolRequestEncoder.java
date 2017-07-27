package com.wifiin.kv.protocol;

import com.wifiin.kv.Command;
import com.wifiin.kv.DataType;
import com.wifiin.util.message.Output;

/**
 * request: UUID_IN_16BYTES KEY_LEN KEY_UTF8 TYPE_BYTE  COMMAND_BYTE BODY_LEN BODY_BYTES 
 * response: UUID_IN_16BYTES STATUS CONTENT
 * @author Running
 *
 */
public interface ProtocolRequestEncoder{
    public void encode(Output output,String key,DataType dataType,Command command,byte[] body);
}
