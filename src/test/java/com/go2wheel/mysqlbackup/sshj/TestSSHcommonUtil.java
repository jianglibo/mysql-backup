package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;

public class TestSSHcommonUtil extends SshBaseFort {
	
	private List<String> remoteFiles = new ArrayList<>();
	
	@After
	public void after() throws IOException {
		for(String fn : remoteFiles) {
			SSHcommonUtil.runRemoteCommand(sshClient, String.format("rm %s", fn));
		}
		super.after();
	}
	
	
	@Test
	public void tWriteRemoteFile() throws IOException {
		String rfn = "/tmp/hello.txt";
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		
		String content = SSHcommonUtil.getRemoteFileContent(sshClient, rfn);
		assertThat(content, equalTo("abc"));
	}
	
	@Test
	public void tbackupFileExist() throws IOException {
		String rfn = "/tmp/filetobackup.txt";
		remoteFiles.add(rfn);
		remoteFiles.add(rfn + ".1");
		remoteFiles.add(rfn + ".2");
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		SSHcommonUtil.backupFile(sshClient, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommandAndGetList(sshClient, String.format("ls %s", rfn + "*"));
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(2));
		assertThat(fns.get(0), equalTo(rfn));
		assertThat(fns.get(1), equalTo(rfn + ".1"));
	}
	
	@Test
	public void tbackupNotFileExist() throws IOException {
		String rfn = "/tmp/filetobackup.txt";
		remoteFiles.add(rfn);
		remoteFiles.add(rfn + ".1");
		remoteFiles.add(rfn + ".2");
		SSHcommonUtil.backupFile(sshClient, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommandAndGetList(sshClient, String.format("ls %s", rfn + "*"));
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(0));
	}


}
