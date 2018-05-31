package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class Server extends BaseModel {
	
	@NotNull
	private String host;
	
	private int coreNumber;
	
	private int port = 22;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSshKeyFile() {
		return sshKeyFile;
	}

	public void setSshKeyFile(String sshKeyFile) {
		this.sshKeyFile = sshKeyFile;
	}

	private String username = "root";
	private String password;
	
	private String sshKeyFile;
	
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
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, "id", "host");
	}
	
}
