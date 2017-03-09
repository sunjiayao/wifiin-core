package com.wifiin.common;

public interface Decider {
	public boolean decide(String src);
	public boolean executeOnMatch();
}
