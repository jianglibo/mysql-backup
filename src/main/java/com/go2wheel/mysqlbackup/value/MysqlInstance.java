package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class MysqlInstance {
	
	private int port = 3306;
	private String password;
	private String mycnfFile;
	
	private List<String> mycnfContent;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getMycnfFile() {
		return mycnfFile;
	}
	public void setMycnfFile(String mycnfFile) {
		this.mycnfFile = mycnfFile;
	}

	

	public List<String> getMycnfContent() {
		return mycnfContent;
	}

	public void setMycnfContent(List<String> mycnfContent) {
		this.mycnfContent = mycnfContent;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
