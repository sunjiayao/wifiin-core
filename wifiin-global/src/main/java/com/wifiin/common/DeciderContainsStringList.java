package com.wifiin.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wifiin.util.Help;


public class DeciderContainsStringList extends AbstractDecider implements Decider{
	private static final String stringPattern="(;|,)\\s*";
	private String[] stringList;
	private boolean executeOnMatch;
	public DeciderContainsStringList(){}
	public DeciderContainsStringList(String[] list, boolean executeOnMatch){
		this.stringList=list;
		this.executeOnMatch=executeOnMatch;
	}
	public DeciderContainsStringList(String list,boolean executeOnMatch){
		this.executeOnMatch=executeOnMatch;
		this.stringList=list.split(stringPattern);
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
		List list=new ArrayList<String>();
		if(stringList!=null){
			list.addAll(Arrays.asList(Arrays.asList(stringList)));
		}
		list.addAll(Arrays.asList(string.split(stringPattern)));
		this.stringList=(String[])list.toArray(new String[0]);
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
		for(int i=0,l=stringList.length;i<l && !(match=(Help.isNotEmpty(src) && src.contains(stringList[i])));i++);
		return decide(match);
	}
	
	@Override
	public boolean executeOnMatch() {
		return this.executeOnMatch;
	}
}
