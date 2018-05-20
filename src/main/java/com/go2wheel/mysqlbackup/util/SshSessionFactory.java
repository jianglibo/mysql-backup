package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class SshSessionFactory {

	private MyAppSettings appSettings;

	private Logger logger = LoggerFactory.getLogger(SshSessionFactory.class);

	public FacadeResult<Session> getConnectedSession(Box box) {
		JSch jsch=new JSch();
		Session session = null;
		try {
			String userName = box.getUsername();
			String host = box.getHost();
			int port = box.getPort();
			session=jsch.getSession(userName, host, port);
			String knownHosts = appSettings.getSsh().getKnownHosts();
			if (!StringUtil.hasAnyNonBlankWord(knownHosts)) {
				return FacadeResult.showMessage("ssh.auth.noknownhosts");
			}
			
			if (!Files.exists(Paths.get(knownHosts))) {
				return FacadeResult.showMessage("ssh.auth.wrongknownhosts", knownHosts);
			}
			jsch.setKnownHosts(knownHosts);
		
			if (box.canSShKeyAuth()) {
				jsch.addIdentity(box.getSshKeyFile());
				session.connect();
			} else if (box.canPasswordAuth()) {
				session.setPassword(box.getPassword());
				session.connect();
			} else if(appSettings.getSsh().sshIdrsaExists()) {
				jsch.addIdentity(appSettings.getSsh().getSshIdrsa());
				session.connect();
			} else {
				FacadeResult.showMessage("ssh.auth.noway");
			}
		} catch (JSchException e) {
			ExceptionUtil.logErrorException(logger, e);
			try {
				if (session != null) {
					session.disconnect();	
				}
			} catch (Exception e1) {
			}
			if (e.getMessage().contains("Auth fail")) {
				return FacadeResult.unexpectedResult(e, "jsch.connect.authfailed");
			} else if (e.getMessage().contains("Connection timed out")) {
				return FacadeResult.unexpectedResult(e, "jsch.connect.failed");
			} else {
				return FacadeResult.unexpectedResult(e, "jsch.connect.failed");
			}
		}
		return FacadeResult.doneExpectedResult(session, CommonActionResult.DONE);
	}
	
	public FacadeResult<Session> getConnectedSession(String username, String host, int port, File sshKeyFile, File knownHosts, String password) {
		JSch jsch=new JSch();
		Session session = null;
		try {
			session=jsch.getSession(username, host, port);
			jsch.setKnownHosts(knownHosts.getAbsolutePath());
			if (sshKeyFile != null) {
				jsch.addIdentity(sshKeyFile.getAbsolutePath());
				session.connect();
			} else if (StringUtil.hasAnyNonBlankWord(password)) {
				session.setPassword(password);
				session.connect();
			} else {
				return FacadeResult.showMessage("ssh.auth.noway");
			}
		} catch (JSchException e) {
			ExceptionUtil.logErrorException(logger, e);
			try {
				if (session != null) {
					session.disconnect();	
				}
			} catch (Exception e1) {
			}
			return FacadeResult.unexpectedResult(e);
		}
		return FacadeResult.doneExpectedResult(session, CommonActionResult.DONE);
	}


	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
}
