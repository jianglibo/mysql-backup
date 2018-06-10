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
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.ServerState;

public class TestServerStateService extends JobBaseFort {

	@Autowired
	private ServerStateService serverStateService;

	@Test
	public void t() throws SchedulerException, RunRemoteCommandException, IOException {
		createServer();
		deleteAllJobs();
		createSession();
		
		ServerState ss = serverStateService.createLinuxServerState(server, session);
		
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));
		
		List<ServerState> sssdb = serverStateDbService.findAll();
		
		assertThat(sssdb.size(), equalTo(1));
		
		
		ss = serverStateService.createWinServerState(server, session);
		assertThat(ss.getAverageLoad(), greaterThan(1));
		assertThat(ss.getMemFree(), greaterThan(10L));
		assertThat(ss.getMemUsed(), greaterThan(10L));

	}

}
