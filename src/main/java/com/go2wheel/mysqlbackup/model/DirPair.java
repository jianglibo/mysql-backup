package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class DirPair extends BaseModel {
	
	private Integer getServerId;
	
	private Integer setServerId;
	
	private String newDir;
	
	public String getNewDir() {
		return newDir;
	}

	public void setNewDir(String newDir) {
		this.newDir = newDir;
	}

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
