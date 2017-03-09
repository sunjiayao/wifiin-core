package com.wifiin.common;

public abstract class AbstractDecider implements Decider{
	public AbstractDecider(){}
	public boolean decide(boolean match){
		if(this.executeOnMatch()){
			return match;
		}else{
			return !match;
		}
	}
}
