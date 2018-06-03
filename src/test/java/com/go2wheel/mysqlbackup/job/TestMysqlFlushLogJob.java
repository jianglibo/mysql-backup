package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

public class TestMysqlFlushLogJob extends JobBaseFort {
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Test
	public void tNotReady() throws JobExecutionException {
		createServer();
		createContext();
		mysqlFlushLogJob.execute(context); // cause server was not ready for mysqlbackup.
		mysqlFlushService.count();
		assertThat(mysqlFlushService.count(), equalTo(0L));
	}
	
	@Test
	public void tReady() throws SchedulerException {
		createServer();
		createContext();
		createMysqlIntance();
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		mysqlFlushLogJob.execute(context);
		mysqlFlushService.count();
		assertThat(mysqlFlushService.count(), equalTo(1L));
		
	}


}
