package com.go2wheel.mysqlbackup.installer;

import java.util.List;
import java.util.Map;

public class MysqlInstallInfo implements InstallInfo {
	private boolean installed;

	private String executable;

	private String packageName;

	private String communityRelease;

	private List<String> rfiles;
	private String mysqlv;
	private Map<String, String> variables;

	public List<String> getRfiles() {
		return rfiles;
	}

	public void setRfiles(List<String> rfiles) {
		this.rfiles = rfiles;
	}

	public String getMysqlv() {
		return mysqlv;
	}

	public void setMysqlv(String mysqlv) {
		this.mysqlv = mysqlv;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getCommunityRelease() {
		return communityRelease;
	}

	public void setCommunityRelease(String communityRelease) {
		this.communityRelease = communityRelease;
	}

	@Override
	public String getVersion() {
		return null;
	}
}
