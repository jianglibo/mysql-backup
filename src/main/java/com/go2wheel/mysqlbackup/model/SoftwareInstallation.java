package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class SoftwareInstallation extends BaseModel {
	
	
	@NotNull
	private Integer serverId;
	
	@NotNull
	private Integer SoftwareId;
	
	private List<String> settings = new ArrayList<>();

	public List<String> getSettings() {
		return settings;
	}
	public void setSettings(List<String> settings) {
		this.settings = settings;
	}
	
	public Map<String, String> getSettingsMap() {
		return StringUtil.toPair(getSettings());
	}
	
	public static SoftwareInstallation newInstance(Server server, Software software) {
		SoftwareInstallation si = new SoftwareInstallation();
		si.setServerId(server.getId());
		si.setSoftwareId(software.getId());
		return si;
	}
	
	public SoftwareInstallation addSetting(String key, String value) {
		this.settings.add(key + "=" + value);
		return this;
	}
	
	public Integer getServerId() {
		return serverId;
	}
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	public Integer getSoftwareId() {
		return SoftwareId;
	}
	public void setSoftwareId(Integer softwareId) {
		SoftwareId = softwareId;
	}
	
}
