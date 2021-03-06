package com.wifiin.util.net.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
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
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wifiin.common.CommonConstant;
import com.wifiin.common.JSON;
import com.wifiin.exception.JsonGenerationException;
import com.wifiin.log.LoggerFactory;
import com.wifiin.util.Help;
import com.wifiin.util.ShutdownHookUtil;
import com.wifiin.util.net.http.exception.HttpClientException;
import com.wifiin.util.string.ThreadLocalStringBuilder;
/**
 * http代替功能还没有测试。
 * 完成了https和http的get post功能。
 * 因为httpclient不支持GET和DELETE方法带请求体，所以即使调用本类方法添加了请求体，实际发起GET或DELETE请求时也会忽略掉。
 * 目前只支持GET POST PUT DELETE，没有支持其它http请求方法
 * @author Running
 *
 */
public class HttpClient {
    private static final Logger log=LoggerFactory.getLogger(HttpClient.class);
    private static final String USER_AGENT=HTTP.USER_AGENT;
    private static final String DEFAULT_USER_AGENT="WifiinCore-HttpClient";
    private static final String REQUEST_COOKIE_HEADER="Cookie";
    private static final String RESPONSE_COOKIE_HEADER="Set-Cookie";
    private static final String CONTENT_TYPE=HTTP.CONTENT_TYPE;
    private static final String CONNECTION=HTTP.CONN_DIRECTIVE;
    private static final String ACCEPT_CHARSET="Accept-Charset";
    private static final String NO_PROXY="";
    private static Registry<ConnectionSocketFactory> REGISTRY;
    private static final PoolingHttpClientConnectionManager POOLED_CONNECTION_MANAGER;
    private static final Map<String,CloseableHttpClient> HTTP_MAP=Maps.newConcurrentMap();
    private static final RegistryBuilder<ConnectionSocketFactory> REGISTRY_BUILDER = RegistryBuilder.<ConnectionSocketFactory>create(); 
    static{
        try{
//            SSLContext sslContext = SSLContexts.createDefault();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            //信任任何链接  
            TrustStrategy anyTrustStrategy = (x509,s)->{  
                return true;
            };
            SSLContext sslContext = SSLContexts.custom().useProtocol("https").useTLS().loadTrustMaterial(trustStore, anyTrustStrategy).build();  
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
            REGISTRY_BUILDER.register("https", sslSF);
            ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();  
            REGISTRY_BUILDER.register("http", plainSF); 
            REGISTRY = REGISTRY_BUILDER.build();  
            POOLED_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(REGISTRY);
            ShutdownHookUtil.addHook(()->{
                HTTP_MAP.values().forEach((http)->{
                    try{
                        http.close();
                    }catch(IOException e){}
                });
            });
        }catch(Exception e){
            throw new HttpClientException(e);
        }
    }
    private String url;
    private String charset;
    private Map<String,String> cookies;
    private Map<String,String> headers;
    private HttpRequestBase requestData;
    private HttpEntity requestEntity;
    private HttpEntity responseEntity;
    private String proxyHost="";
    private int proxyPort;
    private boolean useProxy;
    private boolean pooled;
    private String contentType;
    private String userAgent;
    private StatusLine status;
    private HttpVersion httpVersion;
    private Header[] responseHeaders;
    private Map<String,List<Header>> responseHeaderMap;
    private HttpClientContext context=HttpClientContext.create();
    private int bufferSize=4128;
    private int retryCount=3;
    private int connectionRequestTimeout=5000;
    private int connectTimeout=5000;
    private int socketTimeout=5000;
    private long connectionLiveMinutes=1;
    private int maxConnTotal=1000;
    private int maxConnPerRoute=1000;
    private long connectLiveTime=5000;
    private LayeredConnectionSocketFactory sslsf;
    private File keyStore;
    private String password;
    
    public void setTrustMaterial(File keyStore,String password) {
        try{
            this.keyStore=keyStore;
            this.password=password;
            SSLContextBuilder builder=org.apache.http.ssl.SSLContexts.custom();
            if(Help.isEmpty(password)){
                builder.loadTrustMaterial(keyStore);
            }else{
                builder.loadTrustMaterial(keyStore,password.toCharArray());
            }
            javax.net.ssl.SSLContext sslContext = builder.useProtocol("https").build();  
            sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
        }catch(Exception e){
            throw new HttpClientException(e);
        }
    }
    
    public int getConnectionRequestTimeout(){
        return connectionRequestTimeout;
    }
    public void setConnectionRequestTimeout(int connectionRequestTimeout){
        this.connectionRequestTimeout=connectionRequestTimeout;
    }
    public int getConnectTimeout(){
        return connectTimeout;
    }
    public void setConnectTimeout(int connectTimeout){
        this.connectTimeout=connectTimeout;
    }
    public long getConnectionLiveMinutes(){
        return connectionLiveMinutes;
    }
    public void setConnectionLiveMinutes(long connectionLiveMinutes){
        this.connectionLiveMinutes=connectionLiveMinutes;
    }
    public int getMaxConnTotal(){
        return maxConnTotal;
    }
    public void setMaxConnTotal(int maxConnTotal){
        this.maxConnTotal=maxConnTotal;
    }
    public int getMaxConnPerRoute(){
        return maxConnPerRoute;
    }
    public void setMaxConnPerRoute(int maxConnPerRoute){
        this.maxConnPerRoute=maxConnPerRoute;
    }
    public int getSocketTimeout(){
        return socketTimeout;
    }
    public void setSocketTimeout(int socketTimeout){
        this.socketTimeout=socketTimeout;
    }
    public long getConnectLiveTime(){
        return connectLiveTime;
    }
    public void setConnectLiveTime(long connectLiveTime){
        this.connectLiveTime=connectLiveTime;
    }
    public int getBufferSize(){
        return bufferSize;
    }
    public void setBufferSize(int bufferSize){
        this.bufferSize=bufferSize;
    }
    public int getRetryCount(){
        return retryCount;
    }
    public void setRetryCount(int retryCount){
        this.retryCount=retryCount;
    }
    public HttpVersion getHttpVersion(){
        return httpVersion;
    }
    public void setHttpVersion(HttpVersion version){
        this.httpVersion=version;
    }
    public HttpClient(String url){
        this(url,true);
    }
    public HttpClient(String url,boolean pooled){
        this.url=url;
        this.pooled=pooled;
    }
    
    public InputStream putForInputStream(){
        return access(InputStream.class,HttpMethod.PUT);
    }
    public String put(){
        return access(String.class,HttpMethod.PUT);
    }
    public <R> R put(Class<R> r){
        return access(r,HttpMethod.PUT);
    }
    public void put(OutputStream out){
        access(HttpMethod.PUT,out);
    }
    
    public InputStream deleteForInputStream(){
        return access(InputStream.class,HttpMethod.DELETE);
    }
    public String delete() {
        return access(String.class,HttpMethod.DELETE);
    }
    public <R> R delete(Class<R> r) {
        return access(r,HttpMethod.DELETE);
    }
    public void delete(OutputStream out) {
        access(HttpMethod.DELETE,out);
    }
    
    public InputStream postForInputStream() {
        return access(InputStream.class,HttpMethod.POST);
    }
    public String post() {
        return access(String.class,HttpMethod.POST);
    }
    public <R> R post(Class<R> r) {
        return access(r,HttpMethod.POST);
    }
    public void post(OutputStream out) {
        access(HttpMethod.POST,out);
    }
    
    public InputStream getForInputStream() {
        return access(InputStream.class,HttpMethod.GET);
    }
    public String get() {
        return access(String.class,HttpMethod.GET);
    }
    public <R> R get(Class<R> r){
        return access(r,HttpMethod.GET);
    }
    public <P> void get(OutputStream out) {
        access(HttpMethod.GET,out);
    }
    
    public InputStream optionsForInputStream() {
        return access(InputStream.class,HttpMethod.OPTIONS);
    }
    public String options() {
        return access(String.class,HttpMethod.OPTIONS);
    }
    public <R> R options(Class<R> r) {
        return access(r,HttpMethod.OPTIONS);
    }
    public <P> void options(OutputStream out) {
        access(HttpMethod.OPTIONS,out);
    }
    
    public InputStream traceForInputStream() {
        return access(InputStream.class,HttpMethod.TRACE);
    }
    public String trace() {
        return access(String.class,HttpMethod.TRACE);
    }
    public <R> R trace(Class<R> r) {
        return access(r,HttpMethod.TRACE);
    }
    public <P> void trace(OutputStream out) {
        access(HttpMethod.TRACE,out);
    }
    
    public InputStream headForInputStream() {
        return access(InputStream.class,HttpMethod.HEAD);
    }
    public String head() {
        return access(String.class,HttpMethod.HEAD);
    }
    public <R> R head(Class<R> r) {
        return access(r,HttpMethod.HEAD);
    }
    public <P> void head(OutputStream out) {
        access(HttpMethod.HEAD,out);
    }
    
    public InputStream patchForInputStream() {
        return access(InputStream.class,HttpMethod.PATCH);
    }
    public String patch() {
        return access(String.class,HttpMethod.PATCH);
    }
    public <R> R patch(Class<R> r) {
        return access(r,HttpMethod.PATCH);
    }
    public <P> void patch(OutputStream out) {
        access(HttpMethod.PATCH,out);
    }
    
    public String access(HttpMethod method) {
        return access(method,String.class);
    }
    public <R,P> R access(HttpMethod method,Class<R> r) {
        return communicate(method,r,null);
    }
    public <R> R access(Class<R> r,HttpMethod method){
        return access(method,r);
    }
    public <P> void access(HttpMethod method,OutputStream out) {
        communicate(method,Void.class,out);
    }
    private CloseableHttpClient createHttpClient(HttpClientConnectionManager httpClientConnectionManager){
        HttpClientBuilder httpClientBuilder=HttpClients.custom().setDefaultRequestConfig(
                RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build())
                .setRetryHandler((IOException exception,int executionCount,HttpContext context)->{
                    if (executionCount > retryCount) {
                         return false;
                     }
                     if (exception instanceof org.apache.http.NoHttpResponseException) {
                         return true;
                     }
                     return false;
                })
                .setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(bufferSize).setCharset(Charset.forName(getCharset())).build())
                .setConnectionTimeToLive(connectionLiveMinutes,TimeUnit.MINUTES)
                .setConnectionManager(httpClientConnectionManager)
                .setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute);
        if(sslsf!=null){
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }
        boolean proxy=Help.isNotEmpty(proxyHost) && proxyPort>0;
        if(useProxy || proxy){
            HttpRoutePlanner routePlanner;
            if(proxy){
                HttpHost proxyHost = new HttpHost(this.proxyHost,this.proxyPort);
                routePlanner = new DefaultProxyRoutePlanner(proxyHost);
            }else{
                routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            }
            httpClientBuilder.setRoutePlanner(routePlanner);
        }
        return httpClientBuilder.build();
    }
    private CloseableHttpClient httpClient(){
        if(pooled){
            StringBuilder keyBuilder=ThreadLocalStringBuilder.builder();
            keyBuilder.append(proxyHost).append(":").append(proxyPort).append('#');
            if(keyStore!=null){
                keyBuilder.append(keyStore.getAbsolutePath());
            }
            keyBuilder.append('#').append(password);
            return HTTP_MAP.computeIfAbsent(keyBuilder.toString(),(k)->{
                return createHttpClient(POOLED_CONNECTION_MANAGER);
            });
        }else{
            return createHttpClient(new BasicHttpClientConnectionManager(REGISTRY));
        }
    }
    private void populateRequestHeader(){
        populateRequestCookies();
        addHeader(USER_AGENT,Help.isEmpty(userAgent)?DEFAULT_USER_AGENT:userAgent);
        if(Help.isNotEmpty(contentType)){
            addHeader(CONTENT_TYPE,contentType);
        }
        if(Help.isNotEmpty(this.headers)){
            for(Map.Entry<String,String> entry:headers.entrySet()){
                requestData.setHeader(entry.getKey(),entry.getValue());
            }
        }
        if(httpVersion!=null){
            requestData.setProtocolVersion(httpVersion);
        }
    }
    private void populateRequestCookies(){
        if(Help.isNotEmpty(this.cookies)){
            StringBuilder cookies=ThreadLocalStringBuilder.builder();
            for(Map.Entry<String,String> entry:this.cookies.entrySet()){
                cookies.append(entry.getKey()).append('=').append(entry.getValue()).append(';');
            }
            addHeader(REQUEST_COOKIE_HEADER,cookies.toString());
        }
    }
    private <R,P> R communicate(HttpMethod method,Class<R> r,OutputStream out) {
        CloseableHttpClient http=httpClient();
        requestData=method.method(url,requestEntity);
        populateRequestHeader();
        if(log.isDebugEnabled()){
            log.debug("HttpClient:header:{};{};{};{}",url,requestData.getFirstHeader(USER_AGENT),requestData.getFirstHeader(CONNECTION),requestData.getProtocolVersion());
        }
        try(CloseableHttpResponse response=http.execute(requestData,context)){
            responseEntity=response.getEntity();
            status=response.getStatusLine();
            responseHeaders=response.getAllHeaders();
            return read(r,out);
        }catch(IOException e){
            throw new HttpClientException(e);
        }finally{
            if(!pooled){
                try{
                    http.close();
                }catch(IOException e){
                    throw new HttpClientException(e);
                }
            }
        }
    }
    private <R> R read(Class<R> r,OutputStream out) throws IOException {
        return ReadExecutor.execute(this,r,out);
    }
    
    private enum ReadExecutor{
        RETURN_STRING(String.class) {
            @SuppressWarnings("unchecked")
            @Override
            public <R> R read(HttpClient http,OutputStream out) throws IOException{
                return (R)EntityUtils.toString(http.responseEntity,http.getCharset());
            }
        },RETURN_BYTES(byte[].class) {
            @SuppressWarnings("unchecked")
            @Override
            public <R> R read(HttpClient http,OutputStream out) throws IOException{
                return (R)EntityUtils.toByteArray(http.responseEntity);
            }
        },OUTPUT_TO_OUTPUTSTREAM(Void.class) {
            @Override
            public <R> R read(HttpClient http,OutputStream out) throws IOException{
                if(out!=null){
                    http.responseEntity.writeTo(out);
                    return null;
                }
                throw new IllegalArgumentException("OutputStream arg must not be null");
            }
        },INPUT(InputStream.class){
            @Override
            public <R> R read(HttpClient http,OutputStream out) throws IOException{
                return (R)http.responseEntity.getContent();
            }
            
        },UNSUPPORTABLE(null) {
            @Override
            public <R> R read(HttpClient http,OutputStream out) throws IOException{
                throw new IllegalArgumentException("unsupportable returning type");
            }
        };
        private static final Map<Class<?>,ReadExecutor> READ_EXECUTOR_COLLECTOR;
        static{
            READ_EXECUTOR_COLLECTOR=new HashMap<>();
            ReadExecutor[] res=ReadExecutor.values();
            for(int i=0,l=res.length;i<l;i++){
                ReadExecutor re=res[i];
                Class<?> returnType=re.returnType;
                if(returnType!=null){
                    READ_EXECUTOR_COLLECTOR.put(returnType,re);
                }
            }
        }
        public static ReadExecutor getReadExecutor(Class<?> returnType){
            return Help.convert(READ_EXECUTOR_COLLECTOR.get(returnType),UNSUPPORTABLE);
        }
        public static <R> R execute(HttpClient http,Class<R> r,OutputStream out) throws IOException{
            return getReadExecutor(r).read(http,out);
        }
        private ReadExecutor(Class<?> returnType){
            this.returnType=returnType;
        }
        private Class<?> returnType;
        public abstract <R> R read(HttpClient http,OutputStream out) throws IOException;
    }
    
    public int getResponseCode(){
        return getStatus().getStatusCode();
    }
    public StatusLine getStatus(){
        return status;
    }
    public Header[] getResponseHeaders(){
        return responseHeaders;
    }
    public Map<String,List<Header>> getResponseHeaderMap(){
        if(responseHeaderMap==null){
            responseHeaderMap=Maps.newHashMap();
            for(int i=0,l=responseHeaders.length;i<l;i++){
                Header h=responseHeaders[i];
                List<Header> list=responseHeaderMap.computeIfAbsent(h.getName(),(n)->{return Lists.newArrayList();});
                list.add(h);
            }
        }
        return responseHeaderMap;
    }
    public List<Header> getResponseHeader(String name){
        return getResponseHeaderMap().get(name);
    }
    public String getResponseHeaderValue(String name,int index){
        List<Header> list=getResponseHeader(name);
        if(Help.isEmpty(list)){
            return null;
        }else{
            return list.get(index).getValue();
        }
    }
    public String getResponseFirstHeaderValue(String name){
        return getResponseHeaderValue(name,0);
    }
    public String getResponseLastHeaderValue(String name){
        List<Header> list=getResponseHeader(name);
        if(Help.isEmpty(list)){
            return null;
        }else{
            return list.get(list.size()-1).getValue();
        }
    }
    
    public void setCookies(Map<String,String> cookies) {
        this.cookies = cookies;
    }
    public Map<String,String> getCookie(){
        return cookies;
    }
    public void addCookie(String name,String value) {
        if(this.cookies==null){
            this.cookies=Maps.newHashMap();
        }
        this.cookies.put(name,value);
    }
    public String getCharset() {
        return Help.isEmpty(charset)?CommonConstant.DEFAULT_CHARSET_NAME:charset;
    }
    public void setCharset(String charset) {
        this.charset = charset;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getUrl(){
        return url;
    }
    public void setUrl(String url){
        this.url=url;
    }
    
    public MultipartEntityBuilder multipartEntityBuilder(){
        return new MultipartEntityBuilder(this);
    }
    public void setEntity(HttpEntity entity){
        requestEntity=entity;
    }
    public void inputStreamEntity(InputStream content){
        requestEntity=new InputStreamEntity(content);
    }
    public void stringEntity(String content) throws UnsupportedEncodingException{
        requestEntity=new StringEntity(content,getCharset());
    }
    public void jsonEntity(Object content){
        try{
            stringEntity(JSON.toJSON(content));
        }catch(UnsupportedEncodingException e){
            throw new JsonGenerationException(e);
        }
    }
    public void byteArrayEntity(byte[] content){
        requestEntity=new ByteArrayEntity(content);
    }

    public void addHeader(String name, String value){
        if(headers==null){
            headers=new HashMap<String,String>();
        }
        headers.put(name, value);
    }
    public void proxy(String host,int port){
        this.proxyHost=host;
        this.proxyPort=port;
    }
    public void setUseProxy(boolean use){
        this.useProxy=use;
    }
    
    private static String HTTP_PROXY_HOST="http.proxyHost";
    private static String HTTP_PROXY_PORT="http.proxyPort";
    private static String HTTP_NON_PROXY_HOST="http.nonProxyHosts";
    private static String HTTPS_PROXY_HOST="https.proxyHost";
    private static String HTTPS_PROXY_PORT="https.proxyPort";
    private static Pattern STARTS_WITH_OR_SYMBOL=Pattern.compile("^\\|");
    private static Pattern ENDS_WITH_OR_SYMBOL=Pattern.compile("\\|$");
    private static Pattern MULTI_OR_SYMBOL=Pattern.compile("\\|+");
    private static void addProxyHost(String name,String value){
        Properties props=System.getProperties();
        String host=props.getProperty(name);
        if(Help.isEmpty(host)){
            props.setProperty(name, value);
        }else{
            props.setProperty(name, host+'|'+value);
        }
    }
    private static void setProxyPort(String name, int port){
        System.getProperties().setProperty(name,Integer.toString(port));
    }
    private static void replaceProxyHost(String name, String ip){
        System.getProperties().setProperty(name, ip);
    }
    private static void removeProxyHost(String name,String ip){
        Properties props=System.getProperties();
        String host=props.getProperty(name);
        if(Help.isNotEmpty(host)){
            int start=host.indexOf(ip);
            if(start>=0){
                int end=start+ip.length();
                ip=STARTS_WITH_OR_SYMBOL.matcher(ThreadLocalStringBuilder.builder().append(host).replace(start, end, ip)).replaceFirst("");
                ip=ENDS_WITH_OR_SYMBOL.matcher(ip).replaceFirst("");
                ip=MULTI_OR_SYMBOL.matcher(ip).replaceAll("|");
                props.setProperty(name, ip);
            }
        }
    }
    public static void addHttpProxyHost(String ip){
        addProxyHost(HTTP_PROXY_HOST,ip);
    }
    public static void setHttpProxyPort(int port){
        setProxyPort(HTTP_PROXY_PORT,port);
    }
    public static void replaceHttpProxyHost(String ip){
        replaceProxyHost(HTTP_PROXY_HOST,ip);
    }
    public static void removeHttpProxyHost(String ip){
        removeProxyHost(HTTP_PROXY_HOST,ip);
    }
    public static void removeAllHttpProxy(){
        Properties props=System.getProperties();
        props.remove(HTTP_PROXY_HOST);
        props.remove(HTTP_PROXY_PORT);
    }
    
    public static void addNonHttpProxyHost(String ip){
        addProxyHost(HTTP_NON_PROXY_HOST,ip);
    }
    public static void replaceNonHttpProxyHost(String ip){
        replaceProxyHost(HTTP_NON_PROXY_HOST,ip);
    }
    public static void removeNonHttpProxyHost(String ip){
        removeProxyHost(HTTP_NON_PROXY_HOST,ip);
    }
    public static void removeAllNonHttpProxy(){
        System.getProperties().remove(HTTP_NON_PROXY_HOST);
    }
    
    public static void addHttpsProxyHost(String ip){
        addProxyHost(HTTPS_PROXY_HOST,ip);
    }
    public static void setHttpsProxyPort(int port){
        setProxyPort(HTTPS_PROXY_PORT,port);
    }
    public static void replaceHttpsProxyHost(String ip){
        replaceProxyHost(HTTPS_PROXY_HOST,ip);
    }
    public static void removeHttpsProxyHost(String ip){
        removeProxyHost(HTTPS_PROXY_HOST,ip);
    }
    public static void removeAllHttpsProxy(){
        Properties props=System.getProperties();
        props.remove(HTTPS_PROXY_HOST);
        props.remove(HTTPS_PROXY_PORT);
    }
    public static void removeAllProxy(){
        removeAllHttpProxy();
        removeAllHttpsProxy();
    }
    public static void main(String[] args) {
        System.out.println(new HttpClient("https://www.wifiin.cn").get());
    }
}
