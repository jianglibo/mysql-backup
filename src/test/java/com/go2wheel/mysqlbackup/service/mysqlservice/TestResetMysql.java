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
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
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
	public void a() throws UnExpectedOutputException, JSchException, IOException, AppNotStartedException, UnExpectedInputException {
		confirmPassword(session, server);
	}
	
	private void confirmPassword(Session session, Server server) throws UnExpectedOutputException, JSchException, IOException, UnExpectedInputException {
		if (server == null || session == null)return;
		try {
			MysqlUtil.getDatabases(session, server, server.getMysqlInstance());
			mySqlInstaller.resetPassword(session, server, "1234567", "123456");
			server.getMysqlInstance().setPassword("123456");
		} catch (MysqlAccessDeniedException e) {
		} catch (AppNotStartedException e) {
			mysqlUtil.restartMysql(session, server);
		}
	}
	
	@Test
	public void testRestMysql() throws UnExpectedOutputException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, CommandNotFoundException {
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
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, SchedulerException, CommandNotFoundException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		confirmPassword(session, server);
		boolean b = mySqlInstaller.resetPassword(session, server, "123456", "1234567");
		assertTrue("password should be changed.", b);
		MysqlPasswordReadyExpect<List<String>> mr = new MysqlPasswordReadyExpect<List<String>>(session, server) {
			@Override
			protected void invokeCommandWhichCausePasswordPrompt() throws IOException {
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
