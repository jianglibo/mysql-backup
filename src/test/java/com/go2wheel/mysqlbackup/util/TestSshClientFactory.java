package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;

public class TestSshClientFactory {
	
	private MyAppSettings appSettings;
	private MysqlInstance instance;
	private SshClientFactory scf;
	
	
	@Before
	public void before() throws IOException {
		appSettings = UtilForTe.getMyAppSettings();
		instance = UtilForTe.loadDemoInstance();
		scf = new SshClientFactory();
		scf.setAppSettings(appSettings);
	}
	
	@Test
	public void tPasswordSuccess() throws IOException {
		SSHClient sshClient = scf.getConnectedSSHClient(instance).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tPasswordFailed() throws IOException {
		instance.setPassword("wrongpassword");
		Optional<SSHClient> sshClient = scf.getConnectedSSHClient(instance);
		assertFalse(sshClient.isPresent());
	}
	
	@Test
	public void tSshkeyFileSuccess() throws IOException {
		instance.setSshKeyFile(UtilForTe.getMyAppSettings().getSsh().getSshIdrsa());
		instance.setPassword(null);
		SSHClient sshClient = scf.getConnectedSSHClient(instance).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tGlobalSshkeyFileSuccess() throws IOException {
		instance.setSshKeyFile(null);
		instance.setPassword(null);
		SSHClient sshClient = scf.getConnectedSSHClient(instance).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tGlobalKnownHostsSuccess() throws IOException {
		instance.setSshKeyFile(null);
		instance.setPassword(null);
		instance.setFingerprint(null);
		SSHClient sshClient = scf.getConnectedSSHClient(instance).get();
		UtilForTe.sshEcho(sshClient);
	}
	
	@Test
	public void tFingerPrintFail() throws IOException {
		appSettings.getSsh().setKnownHosts(null);
		instance.setFingerprint(null);
		instance.setSshKeyFile(null);
		instance.setPassword(null);
		assertFalse(scf.getConnectedSSHClient(instance).isPresent());
	}

}
