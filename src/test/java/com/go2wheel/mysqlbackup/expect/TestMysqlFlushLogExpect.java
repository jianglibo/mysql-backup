package com.go2wheel.mysqlbackup.expect;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.MysqlWrongPasswordException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.jcraft.jsch.JSchException;

public class TestMysqlFlushLogExpect extends SshBaseFort {
	
	private String oriPwd;
	
	@Before
	public void before() throws IOException {
		super.before();
		oriPwd = box.getMysqlInstance().getPassword();
	}
	
	@After
	public void after() throws IOException, JSchException, RunRemoteCommandException {
		box.getMysqlInstance().setPassword(oriPwd);
		super.after();
	}
	
	@Test
	public void t() throws Exception {
		MysqlUtil mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		MysqlFlushLogExpect mfe = new MysqlFlushLogExpect(session, box);
		assertTrue(mfe.start().size() == 2);
	}
	
	@Test(expected = MysqlWrongPasswordException.class)
	public void tWrongPassword() throws Exception {
		MysqlUtil mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		box.getMysqlInstance().setPassword("wrongpassword");
		createALocalFile(" ");
		MysqlFlushLogExpect mfe = new MysqlFlushLogExpect(session, box);
		
		assertFalse(mfe.start().size() == 1);
		
	}

}
