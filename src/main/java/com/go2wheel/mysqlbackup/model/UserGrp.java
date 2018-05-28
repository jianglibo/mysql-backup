package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

public class UserGrp extends BaseModel {

	@NotEmpty
	private String ename;
	private String msgkey;
	private Date createdAt;

	public UserGrp() {
		this.createdAt = new Date();
	}

	public UserGrp(String ename, String msgkey) {
		this.createdAt = new Date();
		this.ename = ename;
		this.msgkey = msgkey;
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

}
