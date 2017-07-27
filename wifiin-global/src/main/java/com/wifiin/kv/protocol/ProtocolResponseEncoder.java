package com.wifiin.kv.protocol;

import java.util.UUID;

import com.wifiin.util.message.Output;

/**
 * UUID_IN_16BYTES STATUS CONTENT
 * @author Running
 *
 */
public interface ProtocolResponseEncoder{
    public void encode(Output output,UUID uuid,int status,byte[] content);
    public void encode(Output output,byte[] uuid,int status,byte[] content);
}
