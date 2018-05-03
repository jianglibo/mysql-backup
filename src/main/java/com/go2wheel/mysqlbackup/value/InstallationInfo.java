package com.go2wheel.mysqlbackup.value;

public class InstallationInfo {
	
	private boolean installed;
	private String executable;
	private String version;
	private String failReason;
	
	
	public static InstallationInfo notInstalled() {
		InstallationInfo ii = new InstallationInfo();
		ii.setInstalled(false);
		return ii;
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	
	
	

}
