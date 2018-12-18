package com.go2wheel.mysqlbackup.value;

import java.util.List;

import com.google.common.base.MoreObjects;

public class ServerGrp {

	private String name;

	private String description;

	private List<String> hostnames;

	private List<Server> servers;

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", getName()).add("description", getDescription()).toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Server> getServers() {
		return servers;
	}

	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}
}
