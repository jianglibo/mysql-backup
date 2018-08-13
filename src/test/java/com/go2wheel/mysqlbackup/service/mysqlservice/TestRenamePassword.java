package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import com.go2wheel.mysqlbackup.expect.MysqlPasswordReadyExpect;
import com.jcraft.jsch.JSchException;

public class TestRenamePassword extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 

	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException, SchedulerException {
		clearDb();
		installMysql();
		sdc.setHost(HOST_DEFAULT_GET);
		
		boolean b = mysqlService.resetPassword(session, server, "123456", "1234567");
		
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
