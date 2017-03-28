package com.wifiin.common.query;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wifiin.common.query.exception.QueryException;
import com.wifiin.util.net.http.HttpClient;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.text.template.TextTemplateFormatter;
import com.wifiin.util.text.template.TextTemplateFormatterType;

public interface RemoteQuery{
    public static final Logger log=LoggerFactory.getLogger(RemoteQuery.class);
    public static final TextTemplateFormatter LOG_FORMATTER=TextTemplateFormatterType.PLAIN_TEXT.formatter("CellPhoneHome:request:{};{}","{","}");
    public static final int DEFAULT_TIMEOUT=3000;
    /**
     * 得到查询的url
     * @param param  查询的参数
     * @return
     */
    public String getURL(String param);
    /**
     * 对端响应字符集
     * @return
     */
    public default String getCharset(){
        return "GBK";
    }
    /**
     * 请求查询手机归属地的服务
     * @param param
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public default String request(String param) throws KeyManagementException, NoSuchAlgorithmException, IOException{
        String url=getURL(param);
        HttpClient http=new HttpClient(url);
        http.setConnectTimeout(DEFAULT_TIMEOUT);
        http.setConnectionRequestTimeout(DEFAULT_TIMEOUT);
        http.setCharset(getCharset());
        String response=null;
        try{
            response=http.get();
            return response;
        }finally{
            log.info(LOG_FORMATTER.format(new String[]{url,RegexUtil.replaceAll(response,"\\s","")}));
        }
    }
    /**
     * 解析查询服务的返回结果
     * @param response
     * @return
     * @throws Exception
     */
    public String[] parseResponse(String response)throws Exception;
    /**
     * 查询手机归属地，有些url不返回手机归属市，只返回省
     * @param phone
     * @return
     */
    public default String[] query(String param){
        try{
            return parseResponse(request(param));
        }catch(Exception e){
            throw new QueryException(e);
        }
    }
}
