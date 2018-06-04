package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class ServerGroupContext {
	
	private UserAccount user;
	
	private ServerGrp serverGrp;
	
	private List<ServerContext> serverContexts;
	
	public ServerGroupContext() {}
	
	public ServerGroupContext(List<ServerContext> serverContexts, UserAccount user, ServerGrp serverGrp) {
		super();
		this.user = user;
		this.serverContexts =serverContexts;
		this.serverGrp = serverGrp;
	}
	
	public ServerGrp getServerGrp() {
		return serverGrp;
	}

	public UserAccount getUser() {
		return user;
	}

	public List<ServerContext> getServerContexts() {
		return serverContexts;
	}

	public void setServerContexts(List<ServerContext> serverContexts) {
		this.serverContexts = serverContexts;
	}

	public void setUser(UserAccount user) {
		this.user = user;
	}

	public void setServerGrp(ServerGrp serverGrp) {
		this.serverGrp = serverGrp;
	}

}
