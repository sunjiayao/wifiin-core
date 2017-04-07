package com.wifiin.multilanguage;

import org.springframework.stereotype.Component;

import com.wifiin.multilanguage.Lang;
import com.wifiin.multilanguage.MultiLangMethod;

@Component
public class MultiLangService{
    @MultiLangMethod(app="testSingleArgLang")
    public TestMultiLangModel execute(@Lang String lang){
        return new TestMultiLangModel();
    }
    @MultiLangMethod(app="test2ArgLang")
    public TestMultiLangModel execute(String test,@Lang String lang){
        return new TestMultiLangModel();
    }
    @MultiLangMethod(app="test2ArgLangWithModel")
    public TestMultiLangModel execute(TestMultiLangModel model,@Lang String lang){
        return new TestMultiLangModel();
    }
    @MultiLangMethod(app="testLangInModel")
    public TestMultiLangModel execute(TestMultiLangModelWithLang model){
        return new TestMultiLangModel();
    }
    
}
