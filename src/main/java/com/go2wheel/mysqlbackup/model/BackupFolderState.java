package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class BackupFolderState extends BaseModel {
	
	private Integer backupFolderId;
	private Integer howMany;
	private Integer totalSizeInKb;
	private Date createdAt;
	
	public BackupFolderState() {
		this.createdAt = new Date();
	}
	
	public Integer getBackupFolderId() {
		return backupFolderId;
	}
	public void setBackupFolderId(Integer backupFolderId) {
		this.backupFolderId = backupFolderId;
	}
	public Integer getHowMany() {
		return howMany;
	}
	public void setHowMany(Integer howMany) {
		this.howMany = howMany;
	}
	public Integer getTotalSizeInKb() {
		return totalSizeInKb;
	}
	public void setTotalSizeInKb(Integer totalSizeInKb) {
		this.totalSizeInKb = totalSizeInKb;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	
}

