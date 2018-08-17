package com.go2wheel.mysqlbackup.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class SshSessionFactory {

	public static final String KNOWNHOSTS_NOTEXISTS = "ssh.knownhosts.notexists";
	public static final String SSHKEYFILE_NOTEXISTS = "ssh.keyfile.notexists";

	private MyAppSettings appSettings;

	private Logger logger = LoggerFactory.getLogger(SshSessionFactory.class);

	public FacadeResult<Session> getConnectedSession(Server server) throws JSchException {
		JSch jsch = new JSch();
		Session session = null;
		String userName = server.getUsername();
		String host = server.getHost();
		int port = server.getPort();
		session = jsch.getSession(userName, host, port);
		String knownHosts = appSettings.getSsh().getKnownHosts();
		String idrsaFile = appSettings.getSsh().getSshIdrsa();
		if (!StringUtil.hasAnyNonBlankWord(knownHosts)) {
			return FacadeResult.showMessageUnExpected("ssh.auth.noknownhosts");
		}
		if (!Files.exists(Paths.get(knownHosts.trim()))) {
			return FacadeResult.showMessageUnExpected(KNOWNHOSTS_NOTEXISTS, knownHosts);
		}
		jsch.setKnownHosts(Paths.get(knownHosts.trim()).toAbsolutePath().toString());

		if (server.canSShKeyAuth()) {
			jsch.addIdentity(Paths.get(server.getSshKeyFile().trim()).toAbsolutePath().toString());
			session.connect();
		} else if (server.canPasswordAuth()) {
			session.setPassword(server.getPassword());
			session.connect();
		} else if (appSettings.getSsh().sshIdrsaExists()) {
			jsch.addIdentity(Paths.get(idrsaFile.trim()).toAbsolutePath().toString());
			session.connect();
		} else {
			return FacadeResult.showMessageUnExpected(SSHKEYFILE_NOTEXISTS, idrsaFile);
		}
		return FacadeResult.doneExpectedResult(session, CommonActionResult.DONE);
	}

	public static void closeSession(Session session) {
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
	}

	public FacadeResult<Session> getConnectedSession(String username, String host, int port, File sshKeyFile,
			File knownHosts, String password) {
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(username, host, port);
			jsch.setKnownHosts(knownHosts.getAbsolutePath());
			if (sshKeyFile != null) {
				jsch.addIdentity(sshKeyFile.getAbsolutePath());
				session.connect();
			} else if (StringUtil.hasAnyNonBlankWord(password)) {
				session.setPassword(password);
				session.connect();
			} else {
				return FacadeResult.showMessageUnExpected("ssh.auth.noway");
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
