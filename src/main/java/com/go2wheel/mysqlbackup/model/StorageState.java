package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class StorageState extends BaseModel {
	
	private Integer serverId;
	private String root;
	private long used;
	private long available;

	public Integer getServerId() {
		return serverId;
	}



	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}



	public String getRoot() {
		return root;
	}



	public void setRoot(String root) {
		this.root = root;
	}



	public long getUsed() {
		return used;
	}



	public void setUsed(long used) {
		this.used = used;
	}



	public long getAvailable() {
		return available;
	}



	public void setAvailable(long available) {
		this.available = available;
	}



	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

}
