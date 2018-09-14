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
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;

public class TestFlush extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	
	@Test
	public void testMysqlFlush()
			throws JSchException, IOException, MysqlAccessDeniedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, AppNotStartedException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		FacadeResult<Path> fr = mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		assertTrue(fr.isExpected());
		assertTrue(Files.exists(fr.getResult()));
		
		Path dump = settingsIndb.getCurrentDumpDir(server);
		assertTrue("index file should sit in dump folder.", Files.exists(dump.resolve(fr.getResult().getFileName())));
		
		long fileCount = Files.list(dump).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
		
		long lineInIndexFile = Files.readAllLines(fr.getResult()).size() + 1; // index file it's self.
		
		assertThat(fileCount, equalTo(lineInIndexFile));
	}
	
	
//	@Test
//	public void testEnableBinLog() {
//		FacadeResult<?> fr = mysqlService.disableLogbin(session, server);
//		assertTrue(fr.isExpected());
//		assertFalse(server.getMysqlInstance().getLogBinSetting().isEnabled());
//		
//		fr = mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
//		assertTrue(fr.isExpected());
//		assertTrue(server.getMysqlInstance().getLogBinSetting().isEnabled());
//	}

}
