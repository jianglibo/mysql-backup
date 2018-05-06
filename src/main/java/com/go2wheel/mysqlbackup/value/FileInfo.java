package com.go2wheel.mysqlbackup.value;

import java.nio.file.Path;

public class FileInfo {

	private String rfileAbs;
	private Path lfileAbs;
	private long length;
	private String md5;
	
	private boolean downloaded;
	
	public FileInfo(String[] ss) {
		this.setRfileAbs(ss[1]);
		this.length = Long.valueOf(ss[0]);
	}
	
	public FileInfo(String name, String length) {
		this.setRfileAbs(name);
		this.length = Long.valueOf(length);
	}
	
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getRfileAbs() {
		return rfileAbs;
	}

	public void setRfileAbs(String rfileAbs) {
		this.rfileAbs = rfileAbs;
	}

	public Path getLfileAbs() {
		return lfileAbs;
	}

	public void setLfileAbs(Path lfileAbs) {
		this.lfileAbs = lfileAbs;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
}
