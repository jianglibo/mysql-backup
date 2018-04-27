package com.go2wheel.mysqlbackup.value;

import java.util.List;

public class MysqlInstance {
	
	private int port = 3306;
	private String username;
	private String password;
	private String mycnfFile;
	private String logBin;
	private String logBinBasename;
	private String logBinIndex;
	
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLogBinBasename() {
		return logBinBasename;
	}

	public void setLogBinBasename(String logBinBasename) {
		this.logBinBasename = logBinBasename;
	}

	public String getLogBinIndex() {
		return logBinIndex;
	}

	public void setLogBinIndex(String logBinIndex) {
		this.logBinIndex = logBinIndex;
	}

	public String getLogBin() {
		return logBin;
	}

	public void setLogBin(String logBin) {
		this.logBin = logBin;
	}
}
