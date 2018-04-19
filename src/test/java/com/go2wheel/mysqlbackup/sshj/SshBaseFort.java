package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.YmlConfigFort;

import net.schmizz.sshj.SSHClient;

public class SshBaseFort {
	protected YmlConfigFort c = UtilForTe.getYmlConfigFort();
	
	protected SSHClient sshClient;
	
	private long startTime;
	
	@Before
	public void before() throws IOException {
		sshClient = SshUtilFort.getConnectedSSHClient(c);
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
