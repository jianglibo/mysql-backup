package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestSSHcommonUtil extends SpringBaseFort {
	
	private List<String> remoteFiles = new ArrayList<>();
	
	@After
	public void after() throws IOException, JSchException, RunRemoteCommandException {
		for(String fn : remoteFiles) {
			SSHcommonUtil.runRemoteCommand(session, String.format("rm %s", fn));
		}
	}
	
	@Test
	public void tWriteRemoteFile() throws IOException, ScpException, JSchException {
		String rfn = "/tmp/hello.txt";
		ScpUtil.to(session, rfn, "abc".getBytes());
		ScpUtil.to(session, rfn, "abc".getBytes());
		
		String content = ScpUtil.from(session, rfn).toString();
		assertThat(content, equalTo("abc"));
	}

	@Test
	public void tBackupByMove() throws IOException, ScpException, JSchException, RunRemoteCommandException {
		createADirOnServer(3);
		SSHcommonUtil.runRemoteCommand(session, "rm -rf " + TMP_SERVER_DIR_NAME + ".1");
		SSHcommonUtil.runRemoteCommand(session, "rm -rf " + TMP_SERVER_DIR_NAME + ".2");
		SSHcommonUtil.backupFileByMove(session, TMP_SERVER_DIR_NAME);
		assertFalse(SSHcommonUtil.fileExists(session, TMP_SERVER_DIR_NAME));
		assertTrue(SSHcommonUtil.fileExists(session, TMP_SERVER_DIR_NAME + ".1"));
	}
	
	@Test
	public void tbackupFileExist() throws IOException, JSchException, ScpException, RunRemoteCommandException {
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
	public void tbackupNotFileExist() throws IOException, JSchException, RunRemoteCommandException {
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
	public void testFileExists() throws RunRemoteCommandException {
		boolean b1 = SSHcommonUtil.fileExists(session, "/usr/bin");
		assertTrue(b1);
		boolean b2 = SSHcommonUtil.fileExists(session, "/usr/bin11");
		assertFalse(b2);
	}
	
	@Test
	public void isExecutable() throws RunRemoteCommandException {
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "kkzx");
		assertThat(rcr.getExitValue(), greaterThan(0));
	}
	
	@Test
	public void testCoreNumber() {
		int cores = SSHcommonUtil.coreNumber(session);
		assertThat(cores, greaterThan(0));
	}

}
