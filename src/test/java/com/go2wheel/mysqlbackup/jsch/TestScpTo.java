package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.JSchException;

public class TestScpTo extends SshBaseFort {

	@Test
	public void scpToFileToFile() throws IOException, JSchException {
		createALocalFile("abc");
		String rfile = "/tmp/" + tmpFile.getFileName().toString();
		String lfile = tmpFile.toAbsolutePath().toString();

		ScpUtil.to(sshSession, lfile, rfile);
		
		List<String> er = SSHcommonUtil.runRemoteCommandAndGetList(sshSession, String.format("ls -lh %s", rfile));
		assertThat(er.size(), equalTo(1));
		SSHcommonUtil.deleteRemoteFile(sshSession, rfile);
	}
	
	@Test
	public void scpToFileToDir() throws IOException, JSchException {
		createALocalFile("abc");
		String rfile = "/tmp";
		String lfile = tmpFile.toAbsolutePath().toString();

		ScpUtil.to(sshSession, lfile, rfile);
		
		String rfullpath = "/tmp/" + tmpFile.getFileName().toString(); 
		
		List<String> er = SSHcommonUtil.runRemoteCommandAndGetList(sshSession, String.format("ls -lh %s", rfullpath));
		assertThat(er.size(), equalTo(1));
		SSHcommonUtil.deleteRemoteFile(sshSession, rfullpath);
	}
	
	@Test
	public void scpToStringToFile() throws IOException, JSchException {
		String rfile = "/tmp/" + new Random().nextDouble();
		ScpUtil.to(sshSession, rfile, "abc".getBytes());
		List<String> er = SSHcommonUtil.runRemoteCommandAndGetList(sshSession, String.format("ls -lh %s", rfile));
		assertThat(er.size(), equalTo(1));
		
		assertThat(ScpUtil.from(sshSession, rfile), equalTo("abc"));
		SSHcommonUtil.deleteRemoteFile(sshSession, rfile);
	}


}
