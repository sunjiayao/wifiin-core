package com.wifiin.common.cellphone.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wifiin.exception.CellPhoneHomeException;
import com.wifiin.util.net.http.HttpClient;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.text.template.TextTemplateFormatter;
import com.wifiin.util.text.template.TextTemplateFormatterType;

public interface CellPhoneHome{
    public static final Logger log=LoggerFactory.getLogger(CellPhoneHome.class);
    public static final TextTemplateFormatter LOG_FORMATTER=TextTemplateFormatterType.PLAIN_TEXT.formatter("CellPhoneHome:request:{};{}","{","}");
    public static final int DEFAULT_TIMEOUT=3000;
    /**
     * 得到查询手机归属地的url
     * @param phone
     * @return
     */
    public String getURL(String phone);
    /**
     * 对端响应字符集
     * @return
     */
    public default String getCharset(){
        return "GBK";
    }
    /**
     * 请求查询手机归属地的服务
     * @param phone
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public default String request(String phone) throws KeyManagementException, NoSuchAlgorithmException, IOException{
        String url=getURL(phone);
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
    public default String[] query(String phone){
        try{
            return parseResponse(request(phone));
        }catch(Exception e){
            throw new CellPhoneHomeException(e);
        }
    }
}
