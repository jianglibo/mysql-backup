package com.go2wheel.mysqlbackup.value;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigFile {
	
	private String mypath;
	
	private String appName;
	
	private String entryPoint;
	
	private String hostName;
	
	private String serverName;
	
	private int coreNumber;
	
	private String mem;
	
	private String logDir;
	
	private Map<String, String> taskcmd;
	
	private Map<String, List<String>> processBuilderNeededList = new HashMap<>();
	
	/**
	 * taskcmd in configfile as key. and the path is a directory.
	 */
	private Map<String, Path> logDirs = new HashMap<>();
	
	private Map<String, String> crons;
	
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Map<String, String> getTaskcmd() {
		return taskcmd;
	}

	public void setTaskcmd(Map<String, String> taskcmd) {
		this.taskcmd = taskcmd;
	}

	public String getMypath() {
		return mypath;
	}

	public void setMypath(String mypath) {
		this.mypath = mypath;
	}

	public Map<String, String> getCrons() {
		return crons;
	}

	public void setCrons(Map<String, String> crons) {
		this.crons = crons;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Map<String, List<String>> getProcessBuilderNeededList() {
		return processBuilderNeededList;
	}

	public void setProcessBuilderNeededList(Map<String, List<String>> processBuilderNeededList) {
		this.processBuilderNeededList = processBuilderNeededList;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public Map<String, Path> getLogDirs() {
		return logDirs;
	}

	public void setLogDirs(Map<String, Path> logDirs) {
		this.logDirs = logDirs;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getCoreNumber() {
		return coreNumber;
	}

	public void setCoreNumber(int coreNumber) {
		this.coreNumber = coreNumber;
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}
	
	

}
