package com.go2wheel.mysqlbackup.model;

import java.util.Date;

public class JobError extends BaseModel {
	
	private Integer serverId;
	private String messageKey;
	private String messageDetail;
	private Date createdAt;
	
	public JobError() {
		this.createdAt = new Date();
	}
	
	public JobError(Integer serverId, String messageKey, String messageDetail) {
		this.serverId = serverId;
		this.messageDetail = messageDetail;
		this.messageKey = messageKey;
		this.createdAt = new Date();
	}
	
	public Integer getServerId() {
		return serverId;
	}
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	public String getMessageKey() {
		return messageKey;
	}
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
	public String getMessageDetail() {
		return messageDetail;
	}
	public void setMessageDetail(String messageDetail) {
		this.messageDetail = messageDetail;
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
