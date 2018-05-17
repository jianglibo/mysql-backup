package com.go2wheel.mysqlbackup.util;

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
			session=jsch.getSession(box.getUsername(), box.getHost(), box.getPort());
			jsch.setKnownHosts(appSettings.getSsh().getKnownHosts());
		
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
				logger.error("no authentication method found.");
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
