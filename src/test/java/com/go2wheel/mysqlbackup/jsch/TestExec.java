package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.UtilForTe;
import com.jcraft.jsch.JSchException;

public class TestExec extends SpringBaseFort {

	@Test
	public void testSubsystems() {
		assertTrue(true);
	}

	@Test
	public void testExec() throws IOException, JSchException {
		createSession();
		String s = UtilForTe.sshEcho(session, "helo");
		assertThat(s.trim(), equalTo("helo"));
	}
	
	@Test
	public void testExecReuseSession() throws IOException, JSchException {
		createSession();
		String s = UtilForTe.sshEcho(session, "helo");
		s = UtilForTe.sshEcho(session, "helo");
		s = UtilForTe.sshEcho(session, "helo");
		assertThat(s.trim(), equalTo("helo"));
	}

}
