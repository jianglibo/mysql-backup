package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestServerStateService extends JobBaseFort {

	@Autowired
	private ServerStateService serverStateService;

	@Test
	public void tLinux() throws SchedulerException, RunRemoteCommandException, IOException, JSchException, UnExpectedOutputException {
		clearDb();
		createServer();
		deleteAllJobs();
		createSession();
		
		ServerState ss = serverStateService.createLinuxServerState(server, session, true);
		
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));
		
		List<ServerState> sssdb = serverStateDbService.findAll();
		
		assertThat(sssdb.size(), equalTo(1));
		
		
		ss = serverStateService.createWinServerStateLocal(server, session, true);
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));

	}
	
	@Test
	public void tWin() throws SchedulerException, RunRemoteCommandException, IOException, JSchException, UnExpectedOutputException {
		clearDb();
		Server sv = createServer("localhost");
		sv.setOs("win");
		sv.setUsername(System.getProperty("user.name"));
		deleteAllJobs();
		Session sess = createSession(sv);
		
		ServerState ss = serverStateService.createServerState(sv, sess, true);
		
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));
		
		List<ServerState> sssdb = serverStateDbService.findAll();
		
		assertThat(sssdb.size(), equalTo(1));
		
		
		ss = serverStateService.createWinServerStateLocal(sv, sess, true);
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));

	}

}
