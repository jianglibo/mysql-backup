package com.go2wheel.mysqlbackup.value;

import java.util.Map;

public abstract class ConfigFileBase {
	
	private String appName;
	
	private Map<String, String> taskcmd;

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

}
