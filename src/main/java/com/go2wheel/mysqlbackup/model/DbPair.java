package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class DbPair extends BaseModel {
	
	private Integer getServerId;
	
	private Integer setServerId;
	
	
	public Integer getGetServerId() {
		return getServerId;
	}

	public void setGetServerId(Integer getServerId) {
		this.getServerId = getServerId;
	}

	public Integer getSetServerId() {
		return setServerId;
	}

	public void setSetServerId(Integer setServerId) {
		this.setServerId = setServerId;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}
}
