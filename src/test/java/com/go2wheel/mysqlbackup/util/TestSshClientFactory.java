package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.Box;

import net.schmizz.sshj.SSHClient;

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
	public void tPasswordSuccess() throws IOException {
		SSHClient sshClient = scf.getConnectedSSHClient(box).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tPasswordFailed() throws IOException {
		box.setPassword("wrongpassword");
		Optional<SSHClient> sshClient = scf.getConnectedSSHClient(box);
		assertFalse(sshClient.isPresent());
	}
	
	@Test
	public void tSshkeyFileSuccess() throws IOException {
		box.setSshKeyFile(UtilForTe.getMyAppSettings().getSsh().getSshIdrsa());
		box.setPassword(null);
		SSHClient sshClient = scf.getConnectedSSHClient(box).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tGlobalSshkeyFileSuccess() throws IOException {
		box.setSshKeyFile(null);
		box.setPassword(null);
		SSHClient sshClient = scf.getConnectedSSHClient(box).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tGlobalKnownHostsSuccess() throws IOException {
		box.setSshKeyFile(null);
		box.setPassword(null);
		box.setFingerprint(null);
		SSHClient sshClient = scf.getConnectedSSHClient(box).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tFingerPrintFail() throws IOException {
		appSettings.getSsh().setKnownHosts(null);
		box.setFingerprint(null);
		box.setSshKeyFile(null);
		box.setPassword(null);
		assertFalse(scf.getConnectedSSHClient(box).isPresent());
	}

}
