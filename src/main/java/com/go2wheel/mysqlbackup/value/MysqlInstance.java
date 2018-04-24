package com.go2wheel.mysqlbackup.value;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MysqlInstance {
	
	private int port = 3306;
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
}
