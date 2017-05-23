package com.wifiin.httpclient;

import java.io.IOException;
import java.net.ProxySelector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.wifiin.common.GlobalObject;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
import com.wifiin.util.net.http.HttpClient;

//public class HttpClientTest{
//    private static final String USER_AGENT="User-Agent";
//    private static final String DEFAULT_USER_AGENT="WifiinCore-HttpClient";
//    private static final String REQUEST_COOKIE_HEADER="Cookie";
//    private static final String RESPONSE_COOKIE_HEADER="Set-Cookie";
//    private static final String CONTENT_TYPE="Content-Type";
//    private static final String CONNECTION="Connection";
//    private static final String ACCEPT_CHARSET="Accept-Charset";
//    private static final String NO_PROXY="";
//    private static final Registry<ConnectionSocketFactory> REGISTRY;
//    private static final PoolingHttpClientConnectionManager POOLED_CONNECTION_MANAGER;
//    private static final Map<String,CloseableHttpClient> HTTP_MAP=Maps.newConcurrentMap();
//    static{
//        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create(); 
//        SSLContext sslContext = SSLContexts.createDefault();
////      SSLContext sslContext = SSLContexts.custom().useProtocol("https").loadTrustMaterial(trustStore, anyTrustStrategy).build();  
//        LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext);  
//        registryBuilder.register("https", sslSF);  
//        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();  
//        registryBuilder.register("http", plainSF); 
//        REGISTRY = registryBuilder.build();  
//        POOLED_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(REGISTRY);
//        ShutdownHookUtil.addHook(()->{
//            HTTP_MAP.values().forEach((http)->{
//                try{
//                    http.close();
//                }catch(IOException e){}
//            });
//        });
//    }
//    private String url;
//    private String charset;
//    private Map<String,String> cookies;
//    private Map<String,String> headers;
//    private HttpRequestBase requestData;
//    private HttpEntity requestEntity;
//    private HttpEntity responseEntity;
//    private String proxyHost="";
//    private int proxyPort;
//    private boolean useProxy;
//    private boolean pooled;
//    private String contentType;
//    private String userAgent;
//    private StatusLine status;
//    private HttpVersion httpVersion;
//    private Header[] responseHeaders;
//    private Map<String,List<Header>> responseHeaderMap;
//    private HttpClientContext context=HttpClientContext.create();
//    private int bufferSize=4128;
//    private int retryCount=3;
//    private int connectionRequestTimeout=5000;
//    private int connectTimeout=5000;
//    private int socketTimeout=5000;
//    private long connectionLiveMinutes=1;
//    private int maxConnTotal=1000;
//    private int maxConnPerRoute=1000;
//    private long connectLiveTime=5000;
//    @Test
//    public void testHttpClientWithoutPool() throws IOException{
//        HttpClient http=null;
//        Map m=Maps.newHashMap();
//        m.put("a",1);
//        for(int i=0;i<10;i++){
//            http=new HttpClient("http://172.16.1.7:18010/sdk/test.do",false);
////            http.setHttpVersion(HttpVersion.HTTP_1_0);
//            http.addHeader(HTTP.CONN_DIRECTIVE,HTTP.CONN_CLOSE);
//            http.setContentType("application/json; charset=UTF-8");
//            http.addHeader("Hello-Wifiin","World");
//            http.jsonEntity(m);
//            String result=http.post();
//            System.out.println("AAAAAAAAAA"+result+"  "+http.getStatus());
//        }
//    }
//    @Test
//    public void testApacheHttpClient() throws ClientProtocolException, IOException{
//        Map m=Maps.newHashMap();
//        m.put("a",1);
//        for(int i=0;i<10;i++){
//            try(CloseableHttpClient http=createHttpClient(new BasicHttpClientConnectionManager(REGISTRY))){
//                HttpPost post = new HttpPost("http://172.16.1.7:18010/sdk/test.do");
//                post.setEntity(new StringEntity(GlobalObject.getJsonMapper().writeValueAsString(m),StandardCharsets.UTF_8));
//                post.setHeader("Hello","World");
//                post.setHeader(HTTP.CONTENT_TYPE,"application/json; charset=UTF-8");
//                post.setHeader(HTTP.CONN_DIRECTIVE,HTTP.CONN_CLOSE);
//                post.setHeader(HTTP.USER_AGENT,"HttpClient-Test");
//                post.setProtocolVersion(HttpVersion.HTTP_1_0);
//                try(CloseableHttpResponse result=http.execute(post)){
//                    System.out.println("BBBBBBBBBBBB"+EntityUtils.toString(result.getEntity(),StandardCharsets.UTF_8)+"    "+result.getStatusLine());
//                }
//            }
//        }
//        
//    }
//    private CloseableHttpClient createHttpClient(HttpClientConnectionManager httpClientConnectionManager){
//        HttpClientBuilder httpClientBuilder=HttpClients.custom().setDefaultRequestConfig(
//                RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build())
//                .setRetryHandler((IOException exception,int executionCount,HttpContext context)->{
//                    if (executionCount > retryCount) {
//                         return false;
//                     }
//                     if (exception instanceof org.apache.http.NoHttpResponseException) {
//                         return true;
//                     }
//                     return false;
//                })
//                .setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(bufferSize).setCharset(StandardCharsets.UTF_8).build())
//                .setConnectionTimeToLive(connectionLiveMinutes,TimeUnit.MINUTES)
//                .setConnectionManager(httpClientConnectionManager)
//                .setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute);
//        boolean proxy=Help.isNotEmpty(proxyHost) && proxyPort>0;
//        if(useProxy || proxy){
//            HttpRoutePlanner routePlanner;
//            if(proxy){
//                HttpHost proxyHost = new HttpHost(this.proxyHost,this.proxyPort);
//                routePlanner = new DefaultProxyRoutePlanner(proxyHost);
//            }else{
//                routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
//            }
//            httpClientBuilder.setRoutePlanner(routePlanner);
//        }
//        return httpClientBuilder.build();
//    }
//}
