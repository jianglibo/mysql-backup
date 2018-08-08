package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestRestore extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	

	@Test
	public void testMysqlRestore()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException, SchedulerException {
		sdc.setHost(HOST_DEFAULT_GET);
		clearDb();
		
		//init get
		installMysql();
		MysqlUtil.createDatabases(session, server, server.getMysqlInstance(), "aaaaa");
		mysqlService.mysqlDump(session, server);
		
		MysqlUtil.createDatabases(session, server, server.getMysqlInstance(), "bbbb");
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
		
		//init set
		Server targetServer = createServer(HOST_DEFAULT_SET, true);
		createMysqlIntance(targetServer, "654321");
		Session targetSession = createSession(targetServer);
		installMysql(targetSession, targetServer, "654321");
		
		List<MysqlDumpFolder> mss = mysqlService.listDumpFolders(server);
		MysqlDumpFolder mdf = mss.get(0);
		String remoteFolder = mysqlService.uploadDumpFolder(server, targetServer, targetSession, mdf.getFolder());
		
		assertTrue(SSHcommonUtil.fileExists(targetSession, remoteFolder));
		List<LinuxLsl> files = SSHcommonUtil.listRemoteFiles(targetSession, remoteFolder);
		
		assertThat(files.size(), equalTo(1));
		assertThat("uploaded dump sql are equal", (long)files.size(), equalTo(Files.list(mdf.getFolder()).count()));
		
		String dumpfn = RemotePathUtil.getFileName(server.getMysqlInstance().getDumpFileName());
		
		dumpfn = RemotePathUtil.join(remoteFolder, dumpfn);
		
		MysqlInstance sourceMysqlInstance = server.getMysqlInstance();
		MysqlInstance targetMysqlInstance = targetServer.getMysqlInstance();
		
		List<String> dbnames = MysqlUtil.getDatabases(targetSession, targetServer, targetMysqlInstance);
		
		List<String> lines = mysqlService.importDumped(targetSession, targetServer, sourceMysqlInstance, targetMysqlInstance, remoteFolder);
		
		assertThat(lines.size(), equalTo(1));
		assertTrue(lines.get(0).isEmpty());
		
		List<String> after_dbnames = MysqlUtil.getDatabases(targetSession, targetServer, targetMysqlInstance);
		
		assertThat(after_dbnames.size() - 1, equalTo(dbnames.size()));
		
	}
	
	/*
	 * mysqlbinlog binlog.000001 binlog.000002 | mysql -u root -p
	 * shell> mysqlbinlog binlog.000001 >  /tmp/statements.sql
	 * shell> mysqlbinlog binlog.000002 >> /tmp/statements.sql
	 * shell> mysql -u root -p -e "source /tmp/statements.sql"
	 * 
	 * shell> mysqlbinlog --skip-gtids binlog.000001 >  /tmp/dump.sql
	 * shell> mysqlbinlog --skip-gtids binlog.000002 >> /tmp/dump.sql
	 * shell> mysql -u root -p -e "source /tmp/dump.sql"
	 */

}
