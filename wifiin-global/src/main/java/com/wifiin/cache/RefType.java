package com.wifiin.cache;

import com.google.common.cache.CacheBuilder;
import com.wifiin.cache.exception.CacheException;

public enum RefType{
    STRONG {
        @Override
        public <K,V> CacheBuilder<K,V> keyRefType(CacheBuilder<K,V> builder){
            return builder;
        }
        @Override
        public <K,V> CacheBuilder<K,V> valueRefType(CacheBuilder<K,V> builder){
            return builder;
        }
    },
    SOFT {
        @Override
        public <K,V> CacheBuilder<K,V> keyRefType(CacheBuilder<K,V> builder){
            throw new CacheException("soft key ref does not support soft reference");
        }
        @Override
        public <K,V> CacheBuilder<K,V> valueRefType(CacheBuilder<K,V> builder){
            return builder.softValues();
        }
    },
    WEAK {
        @Override
        public <K,V> CacheBuilder<K,V> keyRefType(CacheBuilder<K,V> builder){
            return builder.weakKeys();
        }
        @Override
        public <K,V> CacheBuilder<K,V> valueRefType(CacheBuilder<K,V> builder){
            return builder.weakValues();
        }
    };
    
    public abstract <K,V> CacheBuilder<K,V> keyRefType(CacheBuilder<K,V> builder);
    public abstract <K,V> CacheBuilder<K,V> valueRefType(CacheBuilder<K,V> builder);
}
