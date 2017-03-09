package com.wifiin.util.xml;

public class XSLTExtensions {
	public static boolean matches(String regex, String src){
		return src.matches(regex);
	}
	public static boolean unmatches(String regex, String src){
		return !matches(regex,src);
	}
}