package com.fujun.browser.model.entity;

public class Table {
	
	private String title;
	private String titleUrl;
	
	private String[] urls;
	private String[] contents;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitleUrl(){
		return titleUrl;
	}
	public void setTitleUrl(String titleUrl){
		this.titleUrl = titleUrl;
	}
	public String[] getUrls() {
		return urls;
	}
	public void setUrls(String[] urls) {
		this.urls = urls;
	}
	public String[] getContents() {
		return contents;
	}
	public void setContents(String[] contents) {
		this.contents = contents;
	}
	
}
