package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class MailServerGrp extends BaseModel {

	private Integer mailAddressId;
	private Integer serverGrpId;
	private String cronExpression;
	private Date createdAt;
	
	public Integer getMailAddressId() {
		return mailAddressId;
	}
	public void setMailAddressId(Integer mailAddressId) {
		this.mailAddressId = mailAddressId;
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
	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
