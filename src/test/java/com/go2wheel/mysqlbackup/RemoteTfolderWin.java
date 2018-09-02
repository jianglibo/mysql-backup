package com.go2wheel.mysqlbackup;

import java.io.IOException;

import org.junit.rules.ExternalResource;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteTfolderWin extends ExternalResource {
	
	private String remoteFolder;
	
	private boolean closeSession;
	

	public String getRemoteFolder() {
		return remoteFolder;
	}

	private Session session;
	
	public RemoteTfolderWin(String remoteFolder, boolean closeSession) {
		this.remoteFolder = remoteFolder.endsWith("/") ? remoteFolder : remoteFolder + "/";
		this.closeSession = closeSession;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public void setRemoteFolder(String remoteFolder) {
		this.remoteFolder = remoteFolder.endsWith("/") ? remoteFolder : remoteFolder + "/";
	}
	
	private boolean goon() {
		return this.session != null&& this.remoteFolder != null && this.remoteFolder.matches("^\\w:.*/.*");
	}
	
	public void createRemoteFolder() throws RunRemoteCommandException, JSchException, IOException {
		if (goon()) {
			SSHcommonUtil.mkdirsp("win", session, remoteFolder);
		}
	}
	
	@Override
	protected void after() {
		if (goon()) {
			try {
				SSHcommonUtil.deleteRemoteFolder("win", session, this.remoteFolder);
			} catch (RunRemoteCommandException | JSchException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (session != null && closeSession) {
			session.disconnect();
		}
	}
	
	public String newFile(String rel) {
		rel = rel.startsWith("/") ? rel.substring(1) : rel;
		return remoteFolder + rel;
	}
	
}
