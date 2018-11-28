package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.List;

import com.go2wheel.mysqlbackup.value.ConfigFile;

public class Server {
	
	private String host;
	
	private String name;
	
	private int coreNumber;
	
	private String os;
	
	private String username = "root";
	
	private List<ConfigFile> configFiles = new ArrayList<>();
	
	private int loadValve = 70;
	private int memoryValve = 70;
	private int diskValve = 70;
	
	public Server() {}
	
	public Server(String host, String name) {
		this.host = host;
		this.name = name;
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
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}
	
	public boolean supportSSH() {
		if (getOs() == null) {
			return true;
		}
		return getOs().contains("linux");
	}

	public int getLoadValve() {
		return loadValve;
	}

	public void setLoadValve(int loadValve) {
		this.loadValve = loadValve;
	}

	public int getMemoryValve() {
		return memoryValve;
	}

	public void setMemoryValve(int memoryValve) {
		this.memoryValve = memoryValve;
	}

	public int getDiskValve() {
		return diskValve;
	}

	public void setDiskValve(int diskValve) {
		this.diskValve = diskValve;
	}

	public List<ConfigFile> getConfigFiles() {
		return configFiles;
	}

	public void setConfigFiles(List<ConfigFile> configFiles) {
		this.configFiles = configFiles;
	}
}
