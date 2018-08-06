package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.jcraft.jsch.JSchException;

public class TestDump extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 

	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException, SchedulerException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
		
		
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		Path localDumpPath = settingsIndb.getCurrentDumpDir(server);
		
		assertThat(localDumpPath.getFileName().toString(), equalTo("dump.0000000"));
		assertTrue(Files.exists(localDumpPath.resolve("mysqldump.sql")));
		
		fr = mysqlService.mysqlDump(session, server, true);
		assertTrue(fr.isExpected());
		
		// after dump, the current dump folder will changed.
		localDumpPath = settingsIndb.getCurrentDumpDir(server);
		
		long pathCount = Files.list(localDumpPath.getParent()).count();
		assertThat(pathCount, equalTo(2L));
		assertTrue(Files.exists(localDumpPath.getParent().resolve("dump.0000001")));
		
		FacadeResult<Path> fr1 =  mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		long fileCount = Files.list(localDumpPath).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
		long lineInIndexFile = Files.readAllLines(fr1.getResult()).size() + 1; // only flush once after dump.
		assertThat(fileCount, equalTo(lineInIndexFile));
		
	}

}
