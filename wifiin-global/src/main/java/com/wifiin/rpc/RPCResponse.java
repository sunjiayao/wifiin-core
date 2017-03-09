package com.wifiin.rpc;

import java.io.Serializable;

public interface RPCResponse extends Serializable{
    public void setStatus(int status);
    public int getStatus();
}
