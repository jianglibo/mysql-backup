package com.go2wheel.mysqlbackup.model;

public class BackupFolderState extends BaseModel {
	
	private Integer backupFolderId;
	private Integer howMany;
	private Integer totalSizeInKb;
	
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

	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

