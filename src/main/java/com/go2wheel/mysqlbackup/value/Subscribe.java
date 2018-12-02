package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class Subscribe {
	
	private String id;
	
	private String username;
	
	private String template;
	
	private String groupname;

	private String cron;
	
	private String description;
	
	private ServerGrp serverGroup;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getGroupname() {
		return groupname;
	}
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getTemplate() {
		if (StringUtil.hasAnyNonBlankWord(template)) {
			return template;
		} else {
			return "ctx.html";
		}
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ServerGrp getServerGroup() {
		return serverGroup;
	}
	public void setServerGroup(ServerGrp serverGroup) {
		this.serverGroup = serverGroup;
	}
}