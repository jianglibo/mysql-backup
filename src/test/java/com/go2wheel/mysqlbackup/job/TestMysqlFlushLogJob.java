package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.jcraft.jsch.JSchException;

public class TestMysqlFlushLogJob extends JobBaseFort {
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Autowired
	private MysqlService mysqlService;
	
	@Autowired
	private MysqlLocalDumpBackupJob mysqlLocalDumpBackupJob;
	
	@Test(expected=ExceptionWrapper.class)
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
	public void tReady() throws SchedulerException, JSchException, NoSuchAlgorithmException, UnExpectedOutputException, UnExpectedInputException, MysqlAccessDeniedException, CommandNotFoundException, IOException, AppNotStartedException, RunRemoteCommandException, ScpException {
		clearDb();
		createSession();
		createContext();
		createMysqlIntance();
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		mysqlService.refreshVariables(session, server);
		mysqlLocalDumpBackupJob.lockerRounded(server, server.getMysqlInstance());
		mysqlFlushLogJob.execute(context);
		mysqlFlushDbService.count();
		assertThat(mysqlFlushDbService.count(), equalTo(1L));
		
	}


}
