package com.wifiin.test.bean;

import com.wifiin.reflect.getset.Getter;

public class TestGetter implements Getter<Test,Integer>{

    @Override
    public Integer get(Test src){
        return src.i;
    }
    
    public static void main(String[] args){
        Getter g=new TestGetter();
        System.out.println(g.get(new Test()));
    }
}
