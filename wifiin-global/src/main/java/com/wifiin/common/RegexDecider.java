package com.wifiin.common;

import java.util.regex.Pattern;

public class RegexDecider extends AbstractDecider implements Decider{
	private Pattern regex;
	private boolean executeOnMeet;
	public RegexDecider(){}
	public RegexDecider(String regex, boolean executeOnMeet){
		this.regex=Pattern.compile(regex);
		this.executeOnMeet=executeOnMeet;
	}
	public Pattern getRegex() {
		return regex;
	}
	public void setRegex(Pattern regex) {
		this.regex = regex;
	}
	public boolean isExecuteOnMeet() {
		return executeOnMeet;
	}
	public void setExecuteOnMeet(boolean executeOnMeet) {
		this.executeOnMeet = executeOnMeet;
	}
	@Override
	public boolean decide(String src) {
		return decide(regex.matcher(src).matches());
	}

	@Override
	public boolean executeOnMatch() {
		return this.executeOnMeet;
	}
}
