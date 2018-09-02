package com.go2wheel.mysqlbackup.win;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.go2wheel.mysqlbackup.RemoteTfolderWin;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestWinssh extends SpringBaseFort {

	@Rule
	public RemoteTfolderWin rfolder = new RemoteTfolderWin("c:/rfolder", true);
	
	private Session localServerSession() throws JSchException {
		Server server = serverDbService.findByHost("localhost");
		if (server == null) {
			server = createServer("localhost");
		}
		server.setUsername(System.getProperty("user.name"));
		return createSession(server);
	}
	
	@Test
	public void tConnect() throws JSchException, RunRemoteCommandException, IOException {
		Session session = localServerSession();
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "echo hello");
		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(1));
		assertThat(rcr.getAllTrimedNotEmptyLines().get(0), equalTo("hello"));
	}
	
	@Test
	public void tFileExists() throws JSchException, RunRemoteCommandException, IOException {
		Session session = localServerSession();
		boolean b = SSHcommonUtil.fileExists("win", session, "c:/kiksiks");
		assertFalse(b);
		b = SSHcommonUtil.fileExists("win", session, "c:/Windows");
		assertTrue(b);
	}
	
	@Test
	public void tGetContent() throws JSchException, IOException, ScpException {
		Session session = localServerSession();
		String c = ScpUtil.from(session, "c:/notexistsfile.txt").toString();
		assertTrue(c.isEmpty());
		
		c = ScpUtil.from(session, "c:/Windows").toString();
		assertTrue(c.isEmpty());
		
		rfolder.setSession(session);
		String f = rfolder.newFile("abc.txt");
		SSHcommonUtil.copy("win", session, f , "abc.".getBytes());

		c = SSHcommonUtil.getContent(session, f);
		assertThat(c, equalTo("abc."));
	}

	
	
	
	@Test
	public void tCopyTo() throws JSchException, RunRemoteCommandException, IOException {
		Session session = localServerSession();
		rfolder.setSession(session);
		String f = rfolder.newFile("abc.txt");
		boolean b = SSHcommonUtil.copy("win", session, f , "abc.".getBytes());
		assertTrue(b);
	}
	
	@Test
	public void tCopyFrom() throws JSchException, RunRemoteCommandException, IOException {
		Session session = localServerSession();
		rfolder.setSession(session);
		String f = rfolder.newFile("abc.txt");
		boolean b = SSHcommonUtil.copy("win", session, f , "abc.".getBytes());
		assertTrue(b);
	}


}
