package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.JSchException;

public class TestScpFrom extends SpringBaseFort {
	
	@Before
	public void before() {
		clearDb();
		createSession();
	}

	@Test
	public void tFromFileToFile() throws IOException, JSchException, ScpException {
		String rfile = "/tmp/xx.txt";
		createAfileOnServer(rfile, "abc");
		createALocalDir();
		String lfile = tmpDirectory.toAbsolutePath().toString();
		ScpUtil.from(session, rfile, lfile);
		Path lf = tmpDirectory.resolve("xx.txt");
		assertTrue(Files.exists(lf));
	}
	
	@Test
	public void tFromFileToString() throws IOException, JSchException, ScpException {
		String rfile = "/tmp/xx.txt";
		createAfileOnServer(rfile, "abc");
		String content = ScpUtil.from(session, rfile).toString();
		assertThat(content.trim(), equalTo("abc"));
	}


}
