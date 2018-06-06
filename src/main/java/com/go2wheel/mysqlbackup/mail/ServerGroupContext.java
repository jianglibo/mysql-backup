package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class ServerGroupContext {
	
	private UserAccount user;
	
	private ServerGrp serverGroup;
	
	private List<ServerContext> servers;
	
	public ServerGroupContext() {}
	
	public ServerGroupContext(List<ServerContext> servers, UserAccount user, ServerGrp serverGroup) {
		super();
		this.user = user;
		this.servers =servers;
		this.serverGroup = serverGroup;
	}


	public ServerGrp getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(ServerGrp serverGroup) {
		this.serverGroup = serverGroup;
	}

	public List<ServerContext> getServers() {
		return servers;
	}

	public void setServers(List<ServerContext> servers) {
		this.servers = servers;
	}

	public UserAccount getUser() {
		return user;
	}


	public void setUser(UserAccount user) {
		this.user = user;
	}
}
