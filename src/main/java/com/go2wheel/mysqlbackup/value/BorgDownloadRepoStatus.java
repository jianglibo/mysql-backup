package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class BorgDownloadRepoStatus {
	
	private int totalFiles;
	
	private int downloadFiles;
	
	private long totalBytes;
	
	private long downloadBytes;
	
	@Override
	public String toString() {
		return ObjectUtil.dumpObjectAsMap(this);
	}

	public int getTotalFiles() {
		return totalFiles;
	}

	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}

	public int getDownloadFiles() {
		return downloadFiles;
	}

	public void setDownloadFiles(int downloadFiles) {
		this.downloadFiles = downloadFiles;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public long getDownloadBytes() {
		return downloadBytes;
	}

	public void setDownloadBytes(long downloadBytes) {
		this.downloadBytes = downloadBytes;
	}

}
