package com.go2wheel.mysqlbackup.value;

public class RemoteFileDescriptionImpl implements RemoteFileDescription {
	
	private String filename;
	private long size;
	
	
	public static RemoteFileDescriptionImpl of(String filename, String size) {
		return new RemoteFileDescriptionImpl(filename, Long.valueOf(size));
	}
	
	private RemoteFileDescriptionImpl(String filename, long size) {
		super();
		this.filename = filename;
		this.size = size;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
}
