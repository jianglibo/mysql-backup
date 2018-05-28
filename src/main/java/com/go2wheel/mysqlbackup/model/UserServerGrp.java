package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class UserServerGrp extends BaseModel {
	private Integer userAccountId;
	private Integer serverGrpId;
	private String cronExpression;
	private Date createdAt;
	
	public Integer getUserAccountId() {
		return userAccountId;
	}
	public void setUserAccountId(Integer userAccountId) {
		this.userAccountId = userAccountId;
	}
	public Integer getServerGrpId() {
		return serverGrpId;
	}
	public void setServerGrpId(Integer serverGrpId) {
		this.serverGrpId = serverGrpId;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
