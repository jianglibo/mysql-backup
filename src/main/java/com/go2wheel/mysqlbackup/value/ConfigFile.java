package com.go2wheel.mysqlbackup.value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigFile {
	
	private String mypath;
	
	private String appName;
	
	private String entryPoint;
	
	private String hostName;
	
	private String logDir;
	
	private Map<String, String> taskcmd;
	
	private Map<String, List<String>> processBuilderNeededList = new HashMap<>();
	
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

}
