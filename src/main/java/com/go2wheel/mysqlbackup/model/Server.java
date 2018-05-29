package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class Server extends BaseModel {
	
	@NotNull
	private String host;
	
	private int coreNumber;
	
	public Server() {}
	
	public Server(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getCoreNumber() {
		return coreNumber;
	}

	public void setCoreNumber(int coreNumber) {
		this.coreNumber = coreNumber;
	}

	@Override
	public String toListRepresentation() {
		return ObjectUtil.toListRepresentation(this, "id", "host");
	}
	
}
