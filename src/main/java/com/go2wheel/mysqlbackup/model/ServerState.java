package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class ServerState extends BaseModel {
	
	private Integer serverId;
	private long memFree;
	private long memUsed;
	private Integer averageLoad = 0;
	
	
	public int memPercent() {
		return (int) (((double)getMemUsed() / (getMemFree() + getMemUsed())) * 100);
	}
	
	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public long getMemFree() {
		return memFree;
	}

	public void setMemFree(long memFree) {
		this.memFree = memFree;
	}

	public long getMemUsed() {
		return memUsed;
	}

	public void setMemUsed(long memUsed) {
		this.memUsed = memUsed;
	}

	public Integer getAverageLoad() {
		return averageLoad;
	}

	public void setAverageLoad(Integer averageLoad) {
		this.averageLoad = averageLoad;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}
}
