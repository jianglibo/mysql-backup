package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotEmpty;

public class UserGrp extends BaseModel {

	@NotEmpty
	private String ename;
	private String msgkey;

	public UserGrp() {
	}

	public UserGrp(String ename, String msgkey) {
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

	@Override
	public String toListRepresentation(String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

}
