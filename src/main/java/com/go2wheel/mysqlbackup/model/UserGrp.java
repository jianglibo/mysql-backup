package com.go2wheel.mysqlbackup.model;

public class UserGrp extends BaseModel {
	
	private String ename;
	private String msgkey;
	private String createdAt;
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
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
