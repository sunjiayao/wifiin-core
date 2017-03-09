package com.wifiin.cache;

import com.wifiin.util.string.ThreadLocalStringBuilder;

public class CacheKeyGenerator{
    private static final String CACHE_KEY_SPLITTER=":";
    public static String generateKey(String prefix,Object... tag){
        StringBuilder builder=ThreadLocalStringBuilder.builder().append(prefix).append(CACHE_KEY_SPLITTER);
        for(int i=0,l=tag.length;i<l;i++){
            builder.append(tag[i]);
            if(i<l-1){
                builder.append(CACHE_KEY_SPLITTER);
            }
        }
        return builder.toString();
    }
}
