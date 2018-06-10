package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.job.ServerStateJob;
import com.go2wheel.mysqlbackup.model.ServerState;

public class TestServerStateDbService extends JobBaseFort {

	@Autowired
	private ServerStateJob serverStateJob;

	@Test
	public void t() throws SchedulerException {
		createServer();
		createContext();
		deleteAllJobs();
		
		serverStateJob.execute(context);

		List<ServerState> serverStates = serverStateDbService.getItemsInDays(server, 3);
		int sz = serverStates.size();
		assertThat(sz, equalTo(1));
		
		serverStates = serverStateDbService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.ServerState.SERVER_STATE.SERVER_ID.eq(server.getId()), 0, 10);
		sz = serverStates.size();
		assertThat(sz, equalTo(1));

		serverStateJob.execute(context);
		serverStates = serverStateDbService.getItemsInDays(server, 3);
		sz = serverStates.size();

		assertThat(sz, equalTo(2));
		
		serverStates = serverStateDbService.getRecentItems(server, 30);
		assertThat(sz, equalTo(2));

	}

}
