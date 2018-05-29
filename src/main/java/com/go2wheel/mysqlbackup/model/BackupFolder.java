package com.go2wheel.mysqlbackup.model;

public class BackupFolder extends BaseModel {
	
	private Integer serverId;
	
	private String folder;
	
	public BackupFolder() {}
	
	public BackupFolder(Integer serverId, String folder) {
		this.serverId = serverId;
		this.folder = folder;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	@Override
	public String toListRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
