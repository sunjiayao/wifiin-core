package com.wifiin.util.charset;

import com.wifiin.util.Help;
import com.wifiin.util.string.ThreadLocalStringBuilder;

public class UnicodeCodec {
	public static String decode(String src,String prefix,String suffix,boolean uppercase){
	    prefix=Help.convert(prefix,"");
        suffix=Help.convert(suffix,"");
	    int start=prefix.length();
	    int step=start+suffix.length();
	    StringBuilder builder=ThreadLocalStringBuilder.builder();
        for(int i=start,l=src.length();i<l;i+=step){
            builder.append((char)(Integer.parseInt(src.substring(i,Integer.min(i+4,l)),16)));
        }
        if(uppercase){
            return builder.toString().toUpperCase();
        }
        return builder.toString();
	}
	public static String encode(String src,String prefix,String suffix){
	    prefix=Help.convert(prefix,"");
	    suffix=Help.convert(suffix,"");
	    StringBuilder result=ThreadLocalStringBuilder.builder();
        for(int i=0,l=src.length();i<l;i++){
            char c=src.charAt(i);
            String charUnicode=Integer.toHexString(c);
            result.append(prefix).append(Help.concat("0",4-charUnicode.length())).append(charUnicode).append(suffix);
        }
        return result.toString();
	}
}
