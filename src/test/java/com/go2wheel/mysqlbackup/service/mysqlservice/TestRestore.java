package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestRestore extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	@Rule
	public RemoteTfolder rfRule = new RemoteTfolder("undetermined");
	
	private void resetdb(Session sess, Server sev, MysqlInstance mi) throws UnExpectedOutputException, MysqlAccessDeniedException {
		MysqlUtil.runSql(sess, sev, mi, "drop database aaaaa");		
		MysqlUtil.runSql(sess, sev, mi, "drop database bbbb");
		List<String> dbs = MysqlUtil.getDatabases(sess, sev, mi);
		assertFalse(dbs.contains("aaaaa"));
		assertFalse(dbs.contains("bbbb"));
	}
	

	@Test
	public void testMysqlRestore()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, RunRemoteCommandException, ScpException, CommandNotFoundException {
		sdc.setHost(HOST_DEFAULT_GET);
		clearDb();
		
		installMysql(); // install mysql if not installed.
		resetdb(session, server, server.getMysqlInstance()); // resetdb
		MysqlUtil.createDatabases(session, server, server.getMysqlInstance(), "aaaaa");
		mysqlService.mysqlDump(session, server); // create a database. included in dumpfile.
		
		MysqlUtil.createDatabases(session, server, server.getMysqlInstance(), "bbbb");
		mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server); // include in logbin file.
		
		//init set
		Server targetServer = createServer(HOST_DEFAULT_SET, true);
		createMysqlIntance(targetServer, "123456");
		Session targetSession = createSession(targetServer);
		installMysql(targetSession, targetServer, "123456");
		
		resetdb(targetSession, targetServer, targetServer.getMysqlInstance());
		
		rfRule.setSession(targetSession);
		
		List<MysqlDumpFolder> mss = mysqlService.listDumpFolders(server);
		MysqlDumpFolder mdf = mss.get(0);
		MysqlInstance targetMysqlInstance = targetServer.getMysqlInstance();
		
		List<String> dbnames = MysqlUtil.getDatabases(targetSession, targetServer, targetMysqlInstance);
		
		PlayBack pb = new PlayBack();
		boolean b = mysqlService.restore(pb, server, targetServer, mdf.getFolder().getFileName().toString(), false);
		assertTrue(b);
		
		List<String> after_dbnames = MysqlUtil.getDatabases(targetSession, targetServer, targetMysqlInstance);
		assertThat(after_dbnames.size() - 2, equalTo(dbnames.size()));
		assertTrue(after_dbnames.contains("aaaaa"));
		assertTrue(after_dbnames.contains("bbbb"));
		
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
