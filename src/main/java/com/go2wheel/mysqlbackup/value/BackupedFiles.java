package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class BackupedFiles {
	
	private String origin;
	private boolean originExists;
	private List<String> backups;
	private int nextInt;
	
	public BackupedFiles(String origin) {
		this.origin = origin;
	}
	public boolean isOriginExists() {
		return originExists;
	}
	public void setOriginExists(boolean originExists) {
		this.originExists = originExists;
	}
	public List<String> getBackups() {
		return backups;
	}
	public void setBackups(List<String> backups) {
		this.backups = backups;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public int getNextInt() {
		return nextInt;
	}
	public void setNextInt(int nextInt) {
		this.nextInt = nextInt;
	}
	
	

}
