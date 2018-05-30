package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class ServerGrp extends BaseModel {
	
	@NotEmpty
	private String ename;
	
	private String msgkey;
	private Date createdAt;
	
	public ServerGrp() {
		this.createdAt = new Date();
	}
	
	public ServerGrp(String ename) {
		this.ename = ename;
		this.createdAt = new Date();
	}

	public String getEname() {
		return ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}

	public String getMsgkey() {
		return msgkey;
	}

	public void setMsgkey(String msgkey) {
		this.msgkey = msgkey;
	}

	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toListRepresentation(String... fields) {
		if (fields.length == 0) {
			fields = new String[]{"id", "ename", "msgkey"};
		}
		return ObjectUtil.toListRepresentation(this, fields);
	}
}
