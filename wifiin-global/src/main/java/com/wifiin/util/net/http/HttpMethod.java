package com.wifiin.util.net.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * HTTP请求方法
 * @author Running
 *
 */
public enum HttpMethod{
    GET {
        @Override
        public HttpRequestBase method(String uri,HttpEntity entity){
            return new HttpGet(uri);
        }
    },POST {
        @Override
        public HttpRequestBase method(String uri,HttpEntity entity){
            HttpPost post = new HttpPost(uri);
            if(entity!=null){
                post.setEntity(entity);
            }
            return post;
        }
    },PUT {
        @Override
        public HttpRequestBase method(String uri,HttpEntity entity){
            HttpPut put=new HttpPut(uri);
            if(entity!=null){
                put.setEntity(entity);
            }
            return put;
        }
    },DELETE {
        @Override
        public HttpRequestBase method(String uri,HttpEntity entity){
            return new HttpDelete(uri);
        }
    };
    public abstract HttpRequestBase method(String uri,HttpEntity entity);
}
