package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.YmlConfigFort;
import com.go2wheel.mysqlbackup.util.SshClientFactory;

import net.schmizz.sshj.SSHClient;

public class SshBaseFort {
	protected YmlConfigFort c = UtilForTe.getYmlConfigFort();
	
	protected SSHClient sshClient;
	
	private long startTime;
	
	@Before
	public void before() throws IOException {
		SshClientFactory scf = new SshClientFactory();
		scf.setAppSettings(UtilForTe.getMyAppSettings());
		sshClient = scf.getConnectedSSHClient(c.getDemoinstance());
		startTime = System.currentTimeMillis();
	}

	@After
	public void after() {
		try {
			sshClient.disconnect();
		} catch (IOException e) {
		}
	}
	
	protected void time() {
		System.out.println(String.format("time elapsed: %s ms", System.currentTimeMillis() - startTime));
	}
	

}
