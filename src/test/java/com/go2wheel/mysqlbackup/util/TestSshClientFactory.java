package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestSshClientFactory {
	
	private MyAppSettings appSettings;
	private Box box;
	private SshClientFactory scf;
	
	
	@Before
	public void before() throws IOException {
		appSettings = UtilForTe.getMyAppSettings();
		box = UtilForTe.loadDemoBox();
		scf = new SshClientFactory();
		scf.setAppSettings(appSettings);
	}
	
	@Test
	public void tPasswordSuccess() throws IOException, JSchException {
		Session sshSession = scf.getConnectedSession(box).get();
		UtilForTe.sshEcho(sshSession);
	}
	
	@Test
	public void tPasswordFailed() throws IOException {
		box.setPassword("wrongpassword");
		Optional<Session> sshSession = scf.getConnectedSession(box);
		assertFalse(sshSession.isPresent());
	}
	
	@Test
	public void tSshkeyFileSuccess() throws IOException, JSchException {
		box.setSshKeyFile(UtilForTe.getMyAppSettings().getSsh().getSshIdrsa());
		box.setPassword(null);
		Session sshSession = scf.getConnectedSession(box).get();
		UtilForTe.sshEcho(sshSession);
	}
	
	@Test
	public void tGlobalSshkeyFileSuccess() throws IOException, JSchException {
		box.setSshKeyFile(null);
		box.setPassword(null);
		Session sshSession = scf.getConnectedSession(box).get();
		UtilForTe.sshEcho(sshSession);
	}
	
	@Test
	public void tGlobalKnownHostsSuccess() throws IOException, JSchException {
		box.setSshKeyFile(null);
		box.setPassword(null);
		box.setFingerprint(null);
		Session sshSession = scf.getConnectedSession(box).get();
		UtilForTe.sshEcho(sshSession);
	}
	
	@Test
	public void tFingerPrintFail() throws IOException {
		appSettings.getSsh().setKnownHosts(null);
		box.setFingerprint(null);
		box.setSshKeyFile(null);
		box.setPassword(null);
		assertFalse(scf.getConnectedSession(box).isPresent());
	}

}
