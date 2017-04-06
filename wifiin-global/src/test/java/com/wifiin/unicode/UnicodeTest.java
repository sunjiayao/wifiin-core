package com.wifiin.unicode;

import org.junit.Test;

import com.wifiin.util.charset.UnicodeCodec;

public class UnicodeTest{
    @Test
    public void decode(){
        System.out.println(UnicodeCodec.decodeUTF8("\\xE5\\xBF\\xAB\\xE5\\xB8\\x86","\\x",""));
    }
}
