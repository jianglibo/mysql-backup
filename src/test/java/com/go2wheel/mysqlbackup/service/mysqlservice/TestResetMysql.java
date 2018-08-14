package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.expect.MysqlPasswordReadyExpect;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestResetMysql extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc;
	
	@After
	public void a() throws UnExpectedContentException, JSchException, IOException, AppNotStartedException {
		confirmPassword(session, server);
	}
	
	private void confirmPassword(Session session, Server server) throws UnExpectedContentException, JSchException, IOException {
		if (server == null || session == null)return;
		try {
			MysqlUtil.getDatabases(session, server, server.getMysqlInstance());
			mySqlInstaller.resetPassword(session, server, "1234567", "123456");
			server.getMysqlInstance().setPassword("123456");
		} catch (MysqlAccessDeniedException e) {
		} catch (AppNotStartedException e) {
			mysqlUtil.restartMysql(session);
		}
	}
	
	@Test
	public void testRestMysql() throws UnExpectedContentException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		confirmPassword(session, server);
		mySqlInstaller.resetMysql(session, server, "1234567");
		server.getMysqlInstance().setPassword("1234567");
		MysqlUtil.getDatabases(session, server, server.getMysqlInstance());
	}
	

	@Test(expected=MysqlAccessDeniedException.class)
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException, SchedulerException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		confirmPassword(session, server);
		boolean b = mySqlInstaller.resetPassword(session, server, "123456", "1234567");
		assertTrue("password should be changed.", b);
		MysqlPasswordReadyExpect mr = new MysqlPasswordReadyExpect(session, server) {
			@Override
			protected void tillPasswordRequired() throws IOException {
				String cmd = "mysql -uroot -p -e \"show variables\"";
				expect.sendLine(cmd);
			}
			@Override
			protected List<String> afterLogin() throws IOException, MysqlAccessDeniedException {
				List<String> s =  expectBashPromptAndReturnList();
				checkAccessDenied(s);
				return s;
			}
		};
		mr.start();
	}

}
