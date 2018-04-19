package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHRuntimeException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.TransportException;

public class TestExec extends SshBaseFort {

	// Multiple sessions
	// Session objects are not reusable, so you can only have one
	// command/shell/subsystem via exec(), startShell() or startSubsystem()
	// respectively. But you can start multiple sessions over a single connection.
	
	@Test
	public void testStartShell() throws ConnectionException, TransportException {
		final Session session = sshClient.startSession();
		Shell shell = session.startShell();
		long l = shell.getRemoteWinSize();
		shell.close();
		session.close();
	}
	
	//	Subsystems are a set of remote commands predefined on the server machine so they can be executed conveniently
	@Test
	public void testSubsystems() {
		assertTrue(true);
	}

	@Test
	public void testExec() throws IOException {
		final Session session = sshClient.startSession();
		try {
			final Command cmd = session.exec("cd ~;rm -f testexec.txt;echo 'abc' > testexec.txt;cat testexec.txt");
			String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			assertThat(cmdOut.trim(), equalTo("abc"));
			cmd.join(5, TimeUnit.SECONDS);
			assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
		} finally {
			session.close();
		}
	}

	@Test(expected= SSHRuntimeException.class)
	public void testReuseSession() throws IOException {
		final Session session = sshClient.startSession();
		try {
			final Command cmd = session.exec("cd ~;rm -f testexec.txt;echo 'abc' > testexec.txt;cat testexec.txt");
			String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			assertThat(cmdOut.trim(), equalTo("abc"));
			cmd.join(5, TimeUnit.SECONDS);
			assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
			// then reuse session.
			final Command cmd1 = session.exec("ls -lh");
			assertThat("resuse exit code should be 0.", cmd1.getExitStatus(), equalTo(0));
		} finally {
			session.close();
		}
	}
	
	@Test
	public void multipleSession() throws IOException {
		for(int i =0;i < 10; i++) {
			oneSessionOneCommand();
		}
		time();
	}
	
	private void oneSessionOneCommand() throws IOException {
		final Session session = sshClient.startSession();
		try {
			final Command cmd = session.exec("cd ~;rm -f testexec.txt;echo 'abc' > testexec.txt;cat testexec.txt");
			String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
			assertThat(cmdOut.trim(), equalTo("abc"));
			cmd.join(5, TimeUnit.SECONDS);
			assertThat("exit code should be 0.", cmd.getExitStatus(), equalTo(0));
		} finally {
			session.close();
		}

	}
}
