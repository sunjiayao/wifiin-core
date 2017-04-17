package com.wifiin.multilanguage;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.wifiin.multilanguage.aop.Lang;
import com.wifiin.multilanguage.aop.MultiLangMethod;

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
    
    @MultiLangMethod(app="testSingleArgLang")
    public List<TestMultiLangModel> executeList(@Lang String lang){
        return Lists.newArrayList(new TestMultiLangModel());
    }
    @MultiLangMethod(app="test2ArgLang")
    public List<TestMultiLangModel> executeList(String test,@Lang String lang){
        return Lists.newArrayList(new TestMultiLangModel());
    }
    @MultiLangMethod(app="test2ArgLangWithModel")
    public List<TestMultiLangModel> executeList(TestMultiLangModel model,@Lang String lang){
        return Lists.newArrayList(new TestMultiLangModel());
    }
    @MultiLangMethod(app="testLangInModel")
    public List<TestMultiLangModel> executeList(TestMultiLangModelWithLang model){
        return Lists.newArrayList(new TestMultiLangModel());
    }
    
    
}
