package com.wifiin.common;

import java.util.Arrays;


public class DeciderByStringList extends AbstractDecider implements Decider{
	private static final String stringPattern=";|,|\\s+";
	private String[] stringList;
	private boolean executeOnMatch;
	public DeciderByStringList(){}
	public DeciderByStringList(String[] list, boolean executeOnMatch){
		this.stringList=list;
		this.executeOnMatch=executeOnMatch;
	}
	public DeciderByStringList(String list,boolean executeOnMatch){
		this.executeOnMatch=executeOnMatch;
		this.stringList=list.split(";|,|\\s+");
	}
	public String[] getStringList() {
		return stringList;
	}
	public void setStringList(String[] stringList) {
		this.stringList = stringList;
	}
	public String getString(){
		return Arrays.toString(stringList);
	}
	public void setString(String string){
		this.stringList=string.split(stringPattern);
	}
	public boolean isExecuteOnMatch() {
		return executeOnMatch;
	}
	public void setExecuteOnMatch(boolean executeOnMatch) {
		this.executeOnMatch = executeOnMatch;
	}

	@Override
	public boolean decide(String src) {
		boolean match=false;
		for(int i=0,l=stringList.length;i<l && !(match=stringList[i].equals(src));i++);
		return decide(match);
	}
	
	@Override
	public boolean executeOnMatch() {
		return this.executeOnMatch;
	}

}
