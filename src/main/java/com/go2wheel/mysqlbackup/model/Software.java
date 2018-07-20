package com.go2wheel.mysqlbackup.model;

public class Software extends BaseModel {
	
	private String name;
	private String version;
	private String targetEnv;
	private String website;
	private String dlurl;
	private String installer;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getTargetEnv() {
		return targetEnv;
	}
	public void setTargetEnv(String targetEnv) {
		this.targetEnv = targetEnv;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getDlurl() {
		return dlurl;
	}
	public void setDlurl(String dlurl) {
		this.dlurl = dlurl;
	}
	public String getInstaller() {
		return installer;
	}
	public void setInstaller(String installer) {
		this.installer = installer;
	}
	
	
	
}
