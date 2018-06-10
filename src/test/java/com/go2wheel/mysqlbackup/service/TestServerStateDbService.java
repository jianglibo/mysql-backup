package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.CommonJobDataKey;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.job.ServerStateJob;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.ServerState;

public class TestServerStateDbService extends JobBaseFort {

	@Autowired
	private ServerStateJob serverStateJob;
	
	@Test
	public void tSuccess() throws SchedulerException {
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
	
	@Test
	public void tFail() throws SchedulerException {
		createServer();
		deleteAllJobs();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, 22);
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		serverStateJob.execute(context);
		List<JobLog> jls = jobLogDbService.findAll();
		assertThat(jls.size(), equalTo(1));
		assertThat(jls.get(0).getJobClass(), equalTo(ServerStateJob.class.getName()));

	}


}
