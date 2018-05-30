package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class ServerGroupContext {
	
	private final UserAccount user;
	
	private final ServerGrp serverGrp;
	
	private final List<ServerContext> oscs;
	
	public ServerGroupContext(List<ServerContext> oscs, UserAccount user, ServerGrp serverGrp) {
		super();
		this.oscs = oscs;
		this.user = user;
		this.serverGrp = serverGrp;
	}
	
	public ServerGrp getServerGrp() {
		return serverGrp;
	}

	public UserAccount getUser() {
		return user;
	}

	public List<ServerContext> getOscs() {
		return oscs;
	}


}
