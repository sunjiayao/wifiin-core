package com.wifiin.mq.server.common;

import java.io.Serializable;

import org.rocksdb.Options;

public class ServerParams extends Options implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID=4080540392831835997L;
    private String[] paths;
    public ServerParams(){}
    public String[] getPaths(){
        return paths;
    }
    public void setPaths(String[] paths){
        this.paths=paths;
    }
    
}
