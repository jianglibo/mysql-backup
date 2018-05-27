package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class ServerGrp extends BaseModel {
	
	private String name;
	private Date createdAt;
	
	public ServerGrp() {
		this.createdAt = new Date();
	}
	
	public ServerGrp(String name) {
		this.name = name;
		this.createdAt = new Date();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
