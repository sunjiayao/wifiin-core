package com.wifiin.multilanguage;

@MultiLangMapper
public class TestMultiLangModel{
    private long id=1234;
    private String test="测试";
    private String testOrigin="字段值不变";
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id=id;
    }
    public String getTest(){
        return test;
    }
    public void setTest(String test){
        this.test=test;
    }
    public String getTestOrigin(){
        return testOrigin;
    }
    public void setTestOrigin(String testOrigin){
        this.testOrigin=testOrigin;
    }
    public String toString(){
        return "test="+test+";"+"testOrigin="+testOrigin;
    }
}
