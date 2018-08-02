package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.autoconfigure.LocalDevToolsAutoConfiguration;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestMysqlService extends SpringBaseFort {

	@Autowired
	private MysqlService mysqlService;
	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	@Autowired
	private MySqlInstaller mySqlInstaller;
	
	private Software software;	
	
	@Before
	public void before() throws JSchException, SchedulerException {
		clearDb();
		deleteAllJobs();
		createSession();
		createMysqlIntance();
		
		mySqlInstaller.syncToDb();
		List<Software> sfs = softwareDbService.findByName("MYSQL");
		software = sfs.get(0);
		MysqlInstallInfo ii = (MysqlInstallInfo) mySqlInstaller.install(session, server, software, "123456").getResult();
		assertTrue(ii.isInstalled());
		mysqlService.enableLogbin(session, server);
	}


	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		sdc.setHost(HOST_DEFAULT);
		
		
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
		
		
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		Path localDumpPath = settingsIndb.getDumpDir(server);
		
		assertThat(localDumpPath.getFileName().toString(), equalTo("dump"));
		assertTrue(Files.exists(localDumpPath.resolve("mysqldump.sql")));
		
		fr = mysqlService.mysqlDump(session, server, true);
		assertTrue(fr.isExpected());
		
		long pathCount = Files.list(localDumpPath.getParent()).count();
		assertThat(pathCount, equalTo(2L));
		assertTrue(Files.exists(localDumpPath.getParent().resolve("dump.00")));
		
		FacadeResult<Path> fr1 =  mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		long fileCount = Files.list(localDumpPath).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
		long lineInIndexFile = Files.readAllLines(fr1.getResult()).size() + 1; // index file it's self.
		assertThat(fileCount, equalTo(lineInIndexFile));
		
	}
	
//	@Test
//	public void testMysqlFlush()
//			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
//		sdc.setHost(HOST_DEFAULT);
//		FacadeResult<Path> fr = mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
//		assertTrue(fr.isExpected());
//		assertTrue(Files.exists(fr.getResult()));
//		
//		Path dump = settingsIndb.getDumpDir(server);
//		assertTrue("index file should sit in dump folder.", Files.exists(dump.resolve(fr.getResult().getFileName())));
//		
//		long fileCount = Files.list(dump).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
//		
//		long lineInIndexFile = Files.readAllLines(fr.getResult()).size() + 1; // index file it's self.
//		
//		assertThat(fileCount, equalTo(lineInIndexFile));
//		
//		
//	}
	
	
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
