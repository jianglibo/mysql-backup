package com.go2wheel.mysqlbackup.service.mysqlservice;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.RemoteTfolder;
import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.job.MysqlLocalDumpBackupJob;
import com.jcraft.jsch.JSchException;

public class TestLocalbackupJob extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	public RemoteTfolder rt = new RemoteTfolder("/tmp/mm");
	
	@Autowired
	private MysqlLocalDumpBackupJob mysqlLocalDumpBackupJob;

	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, CommandNotFoundException, RunRemoteCommandException, ScpException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		clearDumpsFolder();
		mysqlLocalDumpBackupJob.lockerRounded(server, server.getMysqlInstance());
		assertDumpFolderNumber(1);
		
		mysqlLocalDumpBackupJob.lockerRounded(server, server.getMysqlInstance());
		assertDumpFolderNumber(2);
		clearDumpsFolder();
	}

}
