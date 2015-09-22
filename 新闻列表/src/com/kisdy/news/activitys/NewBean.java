package com.kisdy.news.activitys;

public class NewBean {
	
	public NewBean(String newImageUrl, String newTile, String newContent) {
		super();
		this.newImageUrl = newImageUrl;
		this.newTile = newTile;
		this.newContent = newContent;
	}
	
	public NewBean() {
		// TODO Auto-generated constructor stub
	}

	private 	String newImageUrl;
	private String newTile;
	private String newContent;
	
	
	public String getNewImageUrl() {
		return newImageUrl;
	}
	public void setNewImageUrl(String newImageUrl) {
		this.newImageUrl = newImageUrl;
	}
	public String getNewTile() {
		return newTile;
	}
	public void setNewTile(String newTile) {
		this.newTile = newTile;
	}
	public String getNewContent() {
		return newContent;
	}
	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}
	
}
