package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.JSchException;

public class TestSSHcommonUtil extends SshBaseFort {
	
	private List<String> remoteFiles = new ArrayList<>();
	
	@After
	public void after() throws IOException, JSchException {
		for(String fn : remoteFiles) {
			SSHcommonUtil.runRemoteCommand(session, String.format("rm %s", fn));
		}
		super.after();
	}
	
	
	@Test
	public void tWriteRemoteFile() throws IOException {
		String rfn = "/tmp/hello.txt";
		ScpUtil.to(session, rfn, "abc".getBytes());
		ScpUtil.to(session, rfn, "abc".getBytes());
		
		String content = ScpUtil.from(session, rfn).toString();
		assertThat(content, equalTo("abc"));
	}
	
	@Test
	public void tbackupFileExist() throws IOException, JSchException {
		String rfn = "/tmp/filetobackup.txt";
		remoteFiles.add(rfn);
		remoteFiles.add(rfn + ".1");
		remoteFiles.add(rfn + ".2");
		ScpUtil.to(session, rfn, "abc".getBytes());
		SSHcommonUtil.backupFile(session, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(2));
		assertThat(fns.get(0), equalTo(rfn));
		assertThat(fns.get(1), equalTo(rfn + ".1"));
		
		
		SSHcommonUtil.revertFile(session, rfn);
		fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(1));
		assertThat(fns.get(0), equalTo(rfn));
	}
	
	
	@Test
	public void tbackupNotFileExist() throws IOException, JSchException {
		String rfn = "/tmp/filetobackup.txt";
		remoteFiles.add(rfn);
		remoteFiles.add(rfn + ".1");
		remoteFiles.add(rfn + ".2");
		SSHcommonUtil.backupFile(session, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(1)); // err output.
	}
	
	@Test
	public void testFileExists() {
		boolean b1 = SSHcommonUtil.fileExists(session, "/usr/bin");
		assertTrue(b1);
		boolean b2 = SSHcommonUtil.fileExists(session, "/usr/bin11");
		assertFalse(b2);
	}


}
