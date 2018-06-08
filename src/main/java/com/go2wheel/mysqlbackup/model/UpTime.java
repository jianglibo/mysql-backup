package com.go2wheel.mysqlbackup.model;

public class UpTime extends BaseModel {
	
	private Integer serverId;
	
	/* 1分钟内的平均负载 */
	private Integer loadOne;
	
	/* 5分钟内的平均负载 */
	private Integer loadFive;
	
	/* 15分钟内的平均负载 */
	private Integer loadFifteen;
	
	/* 服务器运行时长， 分钟为单位 */
	private Integer uptimeMinutes;
	
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
	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}
}
