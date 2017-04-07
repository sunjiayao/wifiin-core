package com.wifiin.multilanguage.aop.rpc;

import com.wifiin.multilanguage.aop.rpc.model.vo.MultiLangData;
import com.wifiin.multilanguage.aop.rpc.model.vo.MultiLangResponse;

public interface MultiLangRPC{
    public MultiLangResponse queryLang(MultiLangData data);
}
