package com.wifiin.multilanguage.rpc;

import com.wifiin.multilanguage.rpc.model.vo.MultiLangData;
import com.wifiin.multilanguage.rpc.model.vo.MultiLangResponse;

public interface MultiLangRPC{
    public MultiLangResponse queryLang(MultiLangData data);
}
