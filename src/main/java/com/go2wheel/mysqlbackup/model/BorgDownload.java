package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class BorgDownload extends BaseModel {
	
	private Integer serverId;
	private long timeCost;
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

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public long getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(long timeCost) {
		this.timeCost = timeCost;
	}

	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}
}
