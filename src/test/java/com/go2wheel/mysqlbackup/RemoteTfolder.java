package com.go2wheel.mysqlbackup;

import org.junit.rules.ExternalResource;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.Session;

public class RemoteTfolder extends ExternalResource {
	
	private String remoteFolder;
	
	public String getRemoteFolder() {
		return remoteFolder;
	}

	private Session session;
	
	public RemoteTfolder(String remoteFolder) {
		this.remoteFolder = remoteFolder.endsWith("/") ? remoteFolder : remoteFolder + "/";
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	protected void after() {
		if (this.session != null&& this.remoteFolder != null && this.remoteFolder.matches("/.*/.*")) {
			SSHcommonUtil.runRemoteCommand(session, String.format("rm -rf %s", this.remoteFolder));
		}
	}
	
	public String newFile(String rel) {
		return remoteFolder + rel;
	}
	
}
