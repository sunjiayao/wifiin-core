package com.wifiin.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.wifiin.util.io.IOUtil;

/**
 * key.type=string|number
 * key=value
 * 
 * key.type=list|set
 * key.0.type=string
 * key.0=stringValue0
 * key.1.type=value1
 * 
 * key.type=map
 * key.k0.type=string
 * key.k0=stringValue0
 * key.k1.type=number
 * key.k1=numberValue
 * key.k2.type=list
 * key.k2.0=
 * 
 * @author Running
 *
 */
public class ConfigPush{
    public static void main(String[] args) throws FileNotFoundException, IOException{
        String path=args[0];
        Properties prop=IOUtil.loadProperties(new File(path));
        prop.entrySet().forEach((e)->{
            String k=e.getKey().toString();
            String v=e.getValue().toString();
            
        });
    }
}
