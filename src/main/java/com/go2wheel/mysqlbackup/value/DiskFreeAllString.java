package com.go2wheel.mysqlbackup.value;

public class DiskFreeAllString {
	
	private String fileSystem = "";
	private String blocks = "";
	private String used = "";
	private String available = "";
	private String use = "";
	private String mountedOn = "";
	
	private DiskFreeAllString() {
	}
	
	public static DiskFreeAllString build(String line) {
		if (line.contains("Use%")) return null;
		String[] ss = line.trim().split("\\s+");
		if (ss.length == 6) {
			DiskFreeAllString du = new DiskFreeAllString();
			du.fileSystem = ss[0];
			du.blocks = ss[1];
			du.used = ss[2];
			du.available = ss[3];
			du.use = ss[4];
			du.mountedOn = ss[5];
			return du;
		}
		return null;
	}
	
	public String getFileSystem() {
		return fileSystem;
	}
	public String getBlocks() {
		return blocks;
	}
	public String getUsed() {
		return used;
	}
	public String getAvailable() {
		return available;
	}
	public String getUse() {
		return use;
	}
	public String getMountedOn() {
		return mountedOn;
	}
	
	

}
