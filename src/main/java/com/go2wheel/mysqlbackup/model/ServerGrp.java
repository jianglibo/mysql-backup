package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotEmpty;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class ServerGrp extends BaseModel {
	
	@NotEmpty
	private String ename;
	
	private String msgkey;
	
	public ServerGrp() {
	}
	
	public ServerGrp(String ename) {
		this.ename = ename;
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
		if (fields.length == 0) {
			fields = new String[]{"id", "ename", "msgkey"};
		}
		return ObjectUtil.toListRepresentation(this, fields);
	}
}
