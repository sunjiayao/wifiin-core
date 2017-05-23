package com.wifiin.test.constant;

import java.io.IOException;
import java.util.Enumeration;

import org.junit.Test;

import com.wifiin.common.CommonConstant;

public class CommonConstantTest{
    @Test
    public void testLoadConstant() throws IOException{
        Enumeration e=CommonConstant.class.getClassLoader().getResources("");
        while(e.hasMoreElements()){
            System.out.println(e.nextElement());
        }
    }
}
