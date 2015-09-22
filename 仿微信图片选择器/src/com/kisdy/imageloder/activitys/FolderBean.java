package com.kisdy.imageloder.activitys;

public class FolderBean {
	
	private String dir; 			//文件夹路径
	private String firstFilePath;   //第一张图片路径 
	private String fileName;	
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getFirstFilePath() {
		return firstFilePath;
	}
	public void setFirstFilePath(String firstFilePath) {
		this.firstFilePath = firstFilePath;
		int  lastindexof=this.firstFilePath.lastIndexOf("/");
		String dir=firstFilePath.substring(0,lastindexof);
		lastindexof=dir.lastIndexOf("/");
		this.fileName=dir.substring(lastindexof+1);
	}
	public String getFileName() {
		return fileName;
	}
	/*  
	 * 文件名可以自动获取
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	*/
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	private int fileCount;
}
