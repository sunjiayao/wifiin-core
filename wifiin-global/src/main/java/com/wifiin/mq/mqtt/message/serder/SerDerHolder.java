package com.wifiin.mq.mqtt.message.serder;

import com.wifiin.mq.mqtt.exception.SerDerException;
import com.wifiin.reflect.ClassForNameMap;
import com.wifiin.util.Help;

public class SerDerHolder{
    private static final SerDerHolder instance=new SerDerHolder();
    private SerDerHolder(){}
    public static SerDerHolder holder(){
        return instance;
    }
    private volatile SerDer serder;
    public SerDer serder(String sdClassName){
        if(Help.isEmpty(sdClassName)){
            return serder();
        }
        return serder(ClassForNameMap.get(sdClassName));
    }
    public SerDer serder(Class<SerDer> sdClass){
        if(sdClass==null){
            return serder();
        }
        try{
            return serder(sdClass.newInstance());
        }catch(InstantiationException | IllegalAccessException e){
            throw new SerDerException(e);
        }
    }
    public SerDer serder(SerDer serder){
        if(this.serder==null){
            synchronized(this){
                if(this.serder==null){
                    this.serder=serder==null?new FstBinarySerDer():serder;
                }
            }
        }
        return this.serder;
    }
    public SerDer serder(){
        return serder((SerDer)null);
    }
}
