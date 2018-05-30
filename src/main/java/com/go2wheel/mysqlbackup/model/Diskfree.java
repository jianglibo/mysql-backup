package com.go2wheel.mysqlbackup.model;

public class Diskfree extends BaseModel {
	
	private Integer serverId;
	
	private String fileSystem;
	private Integer blocks;
	private Integer used;
	private Integer usePercent;
	private String mountedOn;
	private Integer available;
	
	public Integer getServerId() {
		return serverId;
	}
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	public String getFileSystem() {
		return fileSystem;
	}
	public void setFileSystem(String fileSystem) {
		this.fileSystem = fileSystem;
	}
	public Integer getBlocks() {
		return blocks;
	}
	public void setBlocks(Integer blocks) {
		this.blocks = blocks;
	}
	public Integer getUsed() {
		return used;
	}
	public void setUsed(Integer used) {
		this.used = used;
	}
	public Integer getUsePercent() {
		return usePercent;
	}
	public void setUsePercent(Integer usePercent) {
		this.usePercent = usePercent;
	}
	public String getMountedOn() {
		return mountedOn;
	}
	public void setMountedOn(String mountedOn) {
		this.mountedOn = mountedOn;
	}

	public Integer getAvailable() {
		return available;
	}

	public void setAvailable(Integer available) {
		this.available = available;
	}

	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

}
