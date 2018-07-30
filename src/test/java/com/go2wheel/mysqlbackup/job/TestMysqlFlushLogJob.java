package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.jcraft.jsch.JSchException;

public class TestMysqlFlushLogJob extends JobBaseFort {
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Test(expected=UnExpectedInputException.class)
	public void tNotReady() throws SchedulerException, JSchException {
		clearDb();
		createSession();
		deleteAllJobs();
		createContext();
		mysqlFlushLogJob.execute(context); // cause server was not ready for mysqlbackup.
		mysqlFlushDbService.count();
		assertThat(mysqlFlushDbService.count(), equalTo(0L));
	}
	
	@Test
	public void tReady() throws SchedulerException, JSchException {
		clearDb();
		createSession();
		createContext();
		createMysqlIntance();
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		mysqlFlushLogJob.execute(context);
		mysqlFlushDbService.count();
		assertThat(mysqlFlushDbService.count(), equalTo(1L));
		
	}


}
