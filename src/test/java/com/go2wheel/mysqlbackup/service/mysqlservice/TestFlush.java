package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
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
			throws JSchException, IOException, MysqlAccessDeniedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, AppNotStartedException, CommandNotFoundException, RunRemoteCommandException, ScpException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		clearDumpsFolder();
		mysqlService.dump(session, server);
		FacadeResult<Path> fr = mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		assertTrue(fr.isExpected());
		assertTrue(Files.exists(fr.getResult()));
		
		Path dump = settingsIndb.getCurrentDumpDir(server);
		assertTrue("index file should sit in dump folder.", Files.exists(dump.resolve(fr.getResult().getFileName())));
		
		long fileCount = Files.list(dump).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
		
		long lineInIndexFile = Files.readAllLines(fr.getResult()).size() + 1; // index file it's self.
		
		Files.list(dump).forEach(f -> {
			try {
				assertThat(Files.size(f), greaterThan(0L));
			} catch (IOException e) {
				assertTrue(false);
				e.printStackTrace();
			}
		});
		
		assertThat(fileCount, equalTo(lineInIndexFile));
		
//		assertThat(mysqlFlushDbService.count(), equalTo(1L));
	}

}
