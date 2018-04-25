package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.JSchException;

public class TestScpFrom extends SshBaseFort {

	@Test
	public void tFromFileToFile() throws IOException, JSchException {
		String rfile = "/tmp/xx.txt";
		createAfileOnServer(rfile, "abc");
		createALocalDir();
		String lfile = tmpDirectory.toAbsolutePath().toString();
		ScpUtil.from(sshSession, rfile, lfile);
		Path lf = tmpDirectory.resolve("xx.txt");
		assertTrue(Files.exists(lf));
	}
	
	@Test
	public void tFromFileToString() throws IOException, JSchException {
		String rfile = "/tmp/xx.txt";
		createAfileOnServer(rfile, "abc");
		String content = ScpUtil.from(sshSession, rfile);
		assertThat(content.trim(), equalTo("abc"));
	}


}
