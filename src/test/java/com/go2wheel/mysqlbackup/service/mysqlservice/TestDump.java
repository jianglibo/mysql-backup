package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
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

import com.go2wheel.mysqlbackup.RemoteTfolder;
import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteFileDescription;
import com.jcraft.jsch.JSchException;

public class TestDump extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	public RemoteTfolder rt = new RemoteTfolder("/tmp/mm");

	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, CommandNotFoundException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		String rdump = "/tmp/mysqldump.sql";
		
		// delete remote dump file.
		SSHcommonUtil.deleteRemoteFile(server.getOs(), session, rdump);
		assertFalse(SSHcommonUtil.fileExists(server.getOs(), session, rdump));
		
		assertThat(server.getMysqlInstance().getDumpFileName(), equalTo(rdump));
		
		Path localDumpPathBefore = settingsIndb.getCurrentDumpDir(server);
		
		FacadeResult<RemoteFileDescription> fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
		
		// remote dump file should created.
		assertTrue(SSHcommonUtil.fileExists(server.getOs(), session, rdump));
		
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		Path localDumpPath = settingsIndb.getCurrentDumpDir(server);
		
		String localDumpPathString = localDumpPath.getFileName().toString();
		
//		assertThat(localDumpPathString, equalTo(PathUtil.increamentFileName(localDumpPathBefore.getFileName().toString())));
		assertTrue(Files.exists(localDumpPath.resolve("mysqldump.sql")));
		
		fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
		
		// after dump, the current dump folder will changed.
		localDumpPath = settingsIndb.getCurrentDumpDir(server);
		
		assertTrue(Files.exists(localDumpPath));
		
		FacadeResult<Path> fr1 =  mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		long fileCount = Files.list(localDumpPath).filter(p -> !p.getFileName().toString().endsWith(".sql")).count();
		long lineInIndexFile = Files.readAllLines(fr1.getResult()).size() + 1; // only flush once after dump.
		assertThat(fileCount, equalTo(lineInIndexFile));
		
	}
	
	@Test
	public void testAlterNativeDumpFile() throws UnExpectedOutputException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, CommandNotFoundException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		String rdump = "/tmp/mm/mysqldump.sql";
		
		// delete remote dump file.
		SSHcommonUtil.deleteRemoteFile(server.getOs(), session, rdump);
		assertFalse(SSHcommonUtil.fileExists(server.getOs(), session, rdump));
		
		server.getMysqlInstance().setDumpFileName(rdump);
		assertThat(server.getMysqlInstance().getDumpFileName(), equalTo(rdump));
		
		FacadeResult<RemoteFileDescription> fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
		
		// remote dump file should created.
		assertTrue(SSHcommonUtil.fileExists(server.getOs(), session, rdump));
		
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		Path localDumpPath = settingsIndb.getCurrentDumpDir(server);
		assertTrue(Files.exists(localDumpPath.resolve(PathUtil.getFileName(MysqlInstance.FIXED_DUMP_FILE_NAME))));
	}

}
