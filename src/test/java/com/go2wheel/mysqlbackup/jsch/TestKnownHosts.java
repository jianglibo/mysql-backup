package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 *  ssh-keyscan -H -t rsa 192.168.33.110
 * @author admin
 *
 */
public class TestKnownHosts extends SpringBaseFort {


	@Test()
	public void testKnownHostFromFile() throws IOException, JSchException, RunRemoteCommandException, SchedulerException {
		JSch jsch=new JSch();
		createServer();
		deleteAllJobs();
		
		jsch.setKnownHosts(myAppSettings.getSsh().getKnownHosts());
		assertTrue(".known hosts should be exists.", Files.exists(Paths.get(myAppSettings.getSsh().getKnownHosts())));
		HostKeyRepository hkr = jsch.getHostKeyRepository();
		HostKey[] hks = hkr.getHostKey();
		
		assertThat(hks.length, greaterThan(0));
		assertNotNull(hks[0].getFingerPrint(jsch));
		assertNotNull(hks[0].getHost());
		assertNotNull(hks[0].getKey());
		assertNotNull(hks[0].getMarker());
		assertNotNull(hks[0].getType());

		Session session=jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
		session.setPassword("vagrant");
		session.connect();
		List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
		assertThat(sl.size(), greaterThan(2));
		session.disconnect();
	}
	
	@Test()
	public void testKnownHostFromInputStream() throws IOException, JSchException, RunRemoteCommandException, SchedulerException {
		JSch jsch=new JSch();
		clearDb();
		createServer();
		deleteAllJobs();
		try (InputStream is = Files.newInputStream(Paths.get(myAppSettings.getSsh().getKnownHosts()))) {
			jsch.setKnownHosts(is);
			HostKeyRepository hkr = jsch.getHostKeyRepository();
			HostKey[] hks = hkr.getHostKey();
			
			assertThat(hks.length, greaterThan(0));
			assertNotNull(hks[0].getFingerPrint(jsch));
			assertNotNull(hks[0].getHost());
			assertNotNull(hks[0].getKey());
			assertNotNull(hks[0].getMarker());
			assertNotNull(hks[0].getType());

			Session session=jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
			session.setPassword("vagrant");
			session.connect();
			List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
			assertThat(sl.size(), greaterThan(2));
			session.disconnect();
		}
	}

	
	
	@Test()
	public void testPassword() throws IOException, JSchException, RunRemoteCommandException, SchedulerException {
		JSch jsch=new JSch();
		createServer();
		deleteAllJobs();
		jsch.setKnownHosts(myAppSettings.getSsh().getKnownHosts());
		Session session=jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
		session.setPassword("vagrant");
		session.connect();
		List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
		assertThat(sl.size(), greaterThan(2));
		session.disconnect();
	}
	
	@Test()
	public void testIdentitifile() throws IOException, JSchException, RunRemoteCommandException, SchedulerException {
		JSch jsch=new JSch();
		createServer();
		deleteAllJobs();
		jsch.setKnownHosts(myAppSettings.getSsh().getKnownHosts());
		Session session=jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
		jsch.addIdentity(myAppSettings.getSsh().getSshIdrsa());
		session.connect();
		List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
		assertThat(sl.size(), greaterThan(2));
		session.disconnect();
	}
	
	@Test()
	public void testIdentitiBytes() throws IOException, JSchException, RunRemoteCommandException, SchedulerException {
		JSch jsch=new JSch();
		createServer();
		deleteAllJobs();
		jsch.setKnownHosts(myAppSettings.getSsh().getKnownHosts());
		Session session=jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
		byte[] bytes = Files.readAllBytes(Paths.get(myAppSettings.getSsh().getSshIdrsa()));
		jsch.addIdentity(server.getHost(), bytes, (byte[])null, (byte[])null);
		session.connect();
		List<String> sl = SSHcommonUtil.runRemoteCommand(session, "ls -lh /tmp").getAllTrimedNotEmptyLines();
		assertThat(sl.size(), greaterThan(2));
		session.disconnect();
	}
}
