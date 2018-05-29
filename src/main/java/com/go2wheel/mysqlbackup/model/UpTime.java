package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class UpTime extends BaseModel {
	
	private Integer serverId;
	private Integer loadOne;
	private Integer loadFive;
	private Integer loadFifteen;
	private Integer uptimeMinutes;
	private Date createdAt;
	
	public Integer getServerId() {
		return serverId;
	}
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	public Integer getLoadOne() {
		return loadOne;
	}
	public void setLoadOne(Integer loadOne) {
		this.loadOne = loadOne;
	}
	
	

	public Integer getLoadFifteen() {
		return loadFifteen;
	}
	public void setLoadFifteen(Integer loadFifteen) {
		this.loadFifteen = loadFifteen;
	}
	public Integer getUptimeMinutes() {
		return uptimeMinutes;
	}
	public void setUptimeMinutes(Integer uptimeMinutes) {
		this.uptimeMinutes = uptimeMinutes;
	}

	public Integer getLoadFive() {
		return loadFive;
	}
	public void setLoadFive(Integer loadFive) {
		this.loadFive = loadFive;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	@Override
	public String toListRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}
}
