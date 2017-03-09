
package com.wifiin.util.net.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.wifiin.common.CommonConstant;
import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;
/**
 * 本类原名是HttpClient，因没有连接池功能，重命名为HttpClientDeprecated。
 * 现在的HttpClient封装apache httpclient，带有连接池功能。请使用新版HttpClient
 * @author Running
 *
 */
@Deprecated
public class HttpClientDeprecated {
    
    private static final String USER_AGENT="User-Agent";
    private static final String DEFAULT_USER_AGENT="WifiinCore-HttpClient";
    private static final String SET_COOKIE="Set-Cookie";
    private static final String CONTENT_TYPE="Content-Type";
    private static final String ACCEPT_CHARSET="Accept-Charset";
    
    private HttpURLConnection connection;
    private String url;
    private String charset;
    private List<String> cookie;
    private Map<String,String> headers;
    private Proxy proxy;
    private boolean cache;
    /*
     * connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(cache);
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept-Charset", Help.isEmpty(charset)?"UTF-8":charset);
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "meJOR-httpclient");
     * */
    private boolean doInput=true;
    private boolean doOutput=true;
    private int readTimeout;
    private int connectTimeout;
    private boolean instanceFollowRedirects;
    private String acceptCharset;
    private String contentType;
    private String userAgent;
    
    private static final AtomicBoolean CONNECTION_STATIC_SET=new AtomicBoolean(false);
    
    /**
     * 
     * @param sslAlgorithm
     * @param url
     * @param charset
     * @param cache
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws MalformedURLException
     */
    public HttpClientDeprecated(String sslAlgorithm, String url, String charset,boolean cache) throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException{
        create(sslAlgorithm,url,charset,cache);
    }
    public HttpClientDeprecated(String url, String charset) throws MalformedURLException, KeyManagementException, NoSuchAlgorithmException{
        this("TLS",url,charset,false);
    }
    public HttpClientDeprecated(String url, String charset, boolean supportSSL) throws KeyManagementException, NoSuchAlgorithmException{
        if(supportSSL){
            create("TLS",url,charset,false);
        }else{
            create("",url,charset,false);
        }
    }
    private void initConnectionStaticProps(String sslAlgorithm) throws KeyManagementException, NoSuchAlgorithmException{
        if(CONNECTION_STATIC_SET.compareAndSet(false, true)){
            if(Help.isNotEmpty(sslAlgorithm)){
                SSLContext ssl=SSLContext.getInstance(sslAlgorithm);
                ssl.init(null, new TrustManager[]{
                        new X509TrustManager(){
                            public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException {}
                            public void checkServerTrusted(X509Certificate[] chain,String authType) throws CertificateException {}
                            public X509Certificate[] getAcceptedIssuers() {return null;}
                        }
                }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession session) {return true;}
                });
            }
            HttpURLConnection.setFollowRedirects(false);
        }
    }
    private void create(String sslAlgorithm, String url, String charset,boolean cache) throws NoSuchAlgorithmException, KeyManagementException{
        initConnectionStaticProps(sslAlgorithm);
        this.url=url;
        this.charset=charset;
        this.cache=cache;
    }
    private void setRequestHeaders(HttpURLConnection connection){
        if(Help.isNotEmpty(headers)){
            for(Map.Entry<String, String> entry:headers.entrySet()){
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    private void setCookies(HttpURLConnection connection){
        if(cookie!=null){
            for(String c:cookie){
                connection.setRequestProperty("Cookie", c);
            }
        }
    }
    private void openConnection() throws IOException{
        URL url=new URL(this.url);
        if(proxy==null){
            connection=(HttpURLConnection)url.openConnection();
        }else{
            connection=(HttpURLConnection)url.openConnection(proxy);
        }
    }
    private HttpURLConnection createConnection() throws IOException{
        openConnection();
        connection.setDoInput(doInput);
        connection.setDoOutput(doOutput);
        connection.setUseCaches(cache);
        connection.setReadTimeout(readTimeout==0?10000:readTimeout);
        connection.setConnectTimeout(connectTimeout==0?10000:connectTimeout);
        connection.setInstanceFollowRedirects(instanceFollowRedirects);
        connection.setRequestProperty(ACCEPT_CHARSET, Help.isEmpty(charset)?CommonConstant.DEFAULT_CHARSET_NAME:charset);
        connection.setRequestProperty(CONTENT_TYPE,Help.isEmpty(contentType)?"application/x-www-form-urlencoded":contentType);
        connection.setRequestProperty(USER_AGENT, Help.isEmpty(userAgent)?DEFAULT_USER_AGENT:userAgent);
        setCookies(connection);
        setRequestHeaders(connection);
        return connection;
    }
    
    public String put(String content) throws IOException{
        return access(content,HttpMethod.PUT,String.class);
    }
    public <R,P> R put(P content,Class<R> r) throws IOException{
        return access(content,HttpMethod.PUT,r);
    }
    public String put() throws IOException{
        return access(String.class,HttpMethod.PUT);
    }
    public <R> R put(Class<R> r) throws IOException{
        return access(r,HttpMethod.PUT);
    }
    public <P> void put(P content,OutputStream out) throws IOException{
        access(content,HttpMethod.PUT,out);
    }
    public void put(OutputStream out) throws IOException{
        access(HttpMethod.PUT,out);
    }
    
    public String delete(String content) throws IOException{
        return access(content,HttpMethod.DELETE,String.class);
    }
    public <R,P> R delete(P content,Class<R> r) throws IOException{
        return access(content,HttpMethod.DELETE,r);
    }
    public String delete() throws IOException{
        return access(String.class,HttpMethod.DELETE);
    }
    public <R> R delete(Class<R> r) throws IOException{
        return access(r,HttpMethod.DELETE);
    }
    public <P> void delete(P content,OutputStream out) throws IOException{
        access(content,HttpMethod.DELETE,out);
    }
    public void delete(OutputStream out) throws IOException{
        access(HttpMethod.DELETE,out);
    }
    
    public String post(String content) throws IOException{
        return access(content,HttpMethod.POST,String.class);
    }
    public <R,P> R post(P content,Class<R> r) throws IOException{
        return access(content,HttpMethod.POST,r);
    }
    public String post() throws IOException{
        return access(String.class,HttpMethod.POST);
    }
    public <R> R post(Class<R> r) throws IOException{
        return access(r,HttpMethod.POST);
    }
    public <P> void post(P content,OutputStream out) throws IOException{
        access(content,HttpMethod.POST,out);
    }
    public void post(OutputStream out) throws IOException{
        access(HttpMethod.POST,out);
    }
    
    public String get(String content) throws IOException{
        return access(content,HttpMethod.GET,String.class);
    }
    public <R,P> R get(P content,Class<R> r) throws IOException{
        return access(content,HttpMethod.GET,r);
    }
    public String get() throws IOException{
        return access(String.class,HttpMethod.GET);
    }
    public <R> R get(Class<R> r) throws IOException{
        return access(r,HttpMethod.GET);
    }
    public <P> void get(P content,OutputStream out) throws IOException{
        access(content,HttpMethod.GET,out);
    }
    public void get(OutputStream out) throws IOException{
        access(HttpMethod.GET,out);
    }
    
    public String access(String content,HttpMethod method) throws IOException{
        return communicate(content,method,String.class,null);
    }
    public <R,P> R access(P content,HttpMethod method,Class<R> r) throws IOException{
        return communicate(content,method,r,null);
    }
    public String access(HttpMethod method) throws IOException{
        return access(String.class,method);
    }
    public <R> R access(Class<R> r,HttpMethod method) throws IOException{
        return communicate(null,method,r,null);
    }
    public <P> void access(P content,HttpMethod method,OutputStream out) throws IOException{
        communicate(content,method,Void.class,out);
    }
    public void access(HttpMethod method,OutputStream out) throws IOException{
        communicate(null,method,Void.class,out);
    }
    
    
    private <R,P> R communicate(P content, HttpMethod method,Class<R> r,OutputStream out) throws IOException{
        try{
            createConnection();
            connection.setRequestMethod(method.name());
            connection.connect();
            if(content!=null){
                write(connection,content);
            }
            return read(connection,r,out);
        }finally{
            if(connection!=null){
                connection.disconnect();
            }
        }
    }
    private <P> void write(HttpURLConnection connection,P content) throws IOException{
        PrintStream ps=null;
        OutputStream out=null;
        try{
            out=connection.getOutputStream();
            if(content instanceof String){
                ps=new PrintStream(out,true,charset);
                ps.print(content);
            }else if(content instanceof byte[]){
                out.write((byte[])content);
            }else{
                throw new IllegalArgumentException("content type is illegal, only byte[] or String are allowed, but it is "+content.getClass());
            }
        }finally{
            if(ps!=null){
                ps.close();
            }
            if(out!=null){
                out.close();
            }
        }
    }
    private <R> R read(HttpURLConnection connection,Class<R> r,OutputStream out) throws IOException{
        return ReadExecutor.execute(this,connection,r,out);
    }
    
    private enum ReadExecutor{
        RETURN_STRING(String.class) {
            @SuppressWarnings("unchecked")
            @Override
            public <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException{
                return (R)super.readForByteArrayOutputStream(http,connection).toString(http.charset);
            }
        },RETURN_BYTES(byte[].class) {
            @SuppressWarnings("unchecked")
            @Override
            public <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException{
                return (R)super.readForByteArrayOutputStream(http,connection).toByteArray();
            }
        },RETURN_INPUTSTREAM(InputStream.class) {
            @SuppressWarnings("unchecked")
            @Override
            public <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException{
                super.populateCookie(http,connection);
                return (R)connection.getInputStream();
            }
        },OUTPUT_TO_OUTPUTSTREAM(Void.class) {
            @Override
            public <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException{
                if(out!=null){
                    super.readForOutputStream(http,connection,out);
                    return null;
                }
                throw new IllegalArgumentException("OutputStream arg must not be null");
            }
        },UNSUPPORTABLE(null) {
            @Override
            public <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException{
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
        public static <R> R execute(HttpClientDeprecated http,HttpURLConnection connection,Class<R> r,OutputStream out) throws IOException{
            return getReadExecutor(r).read(http,connection,out);
        }
        private ReadExecutor(Class<?> returnType){
            this.returnType=returnType;
        }
        private Class<?> returnType;
        public abstract <R> R read(HttpClientDeprecated http,HttpURLConnection connection,OutputStream out) throws IOException;
        private ByteArrayOutputStream readForByteArrayOutputStream(HttpClientDeprecated http,HttpURLConnection connection) throws IOException{
            ByteArrayOutputStream o=new ByteArrayOutputStream();
            readForOutputStream(http,connection,o);
            return o;
        }
        private void populateCookie(HttpClientDeprecated http, HttpURLConnection connection){
            http.cookie=connection.getHeaderFields().get(SET_COOKIE);
        }
        private void readForOutputStream(HttpClientDeprecated http, HttpURLConnection connection,OutputStream out) throws IOException{
            populateCookie(http,connection);
            InputStream in=null;
            try{
                in=connection.getInputStream();
                int b=-1;
                while((b=in.read()) > -1){
                    out.write(b);
                }
                out.flush();
            }finally{
                if(in!=null){
                    in.close();
                }
            }
        }
        
    }
    
    public int getResponseCode() throws IOException{
        return connection.getResponseCode();
    }
    public void setCookie(List<String> cookie) {
        this.cookie = cookie;
    }
    public List<String> getCookie(){
        return cookie;
    }
    public void addCookie(String cookie) {
        if(this.cookie==null){
            this.cookie=new ArrayList<String>();
        }
        this.cookie.add(cookie);
    }
    public String getCharset() {
        return charset;
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
    public boolean isDoInput() {
        return doInput;
    }
    public void setDoInput(boolean doInput) {
        this.doInput = doInput;
    }
    public boolean isDoOutput() {
        return doOutput;
    }
    public void setDoOutput(boolean doOutput) {
        this.doOutput = doOutput;
    }
    public long getReadTimeout() {
        return readTimeout;
    }
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    public long getConnectTimeout() {
        return connectTimeout;
    }
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    public boolean isInstanceFollowRedirects() {
        return instanceFollowRedirects;
    }
    public void setInstanceFollowRedirects(boolean instanceFollowRedirects) {
        this.instanceFollowRedirects = instanceFollowRedirects;
    }
    public String getAcceptCharset() {
        return acceptCharset;
    }
    public void setAcceptCharset(String acceptCharset) {
        this.acceptCharset = acceptCharset;
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
    public void addHeader(String name, String value){
        if(headers==null){
            headers=new HashMap<String,String>();
        }
        headers.put(name, value);
    }
    public void setProxy(String host,int port){
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host,port));
    }
    
    private static String HTTP_PROXY_HOST="http.proxyHost";
    private static String HTTP_PROXY_PORT="http.proxyPort";
    private static String HTTP_NON_PROXY_HOST="http.nonProxyHosts";
    private static String HTTPS_PROXY_HOST="https.proxyHost";
    private static String HTTPS_PROXY_PORT="https.proxyPort";
    private static Pattern STARTS_WITH_OR_SYMBOL=Pattern.compile("^\\|");
    private static Pattern ENDS_WITH_OR_SYMBOL=Pattern.compile("\\|$");
    private static Pattern DOUBLE_OR_SYMBOL=Pattern.compile("\\|\\|");
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
                ip=DOUBLE_OR_SYMBOL.matcher(ip).replaceAll("");
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
    
}
