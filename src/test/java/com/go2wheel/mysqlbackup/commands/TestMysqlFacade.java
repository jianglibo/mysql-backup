package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.jcraft.jsch.JSchException;

public class TestMysqlFacade extends SshBaseFort {
	
	private MysqlUtil mysqlUtil;
	
	private MysqlTaskFacade mysqlTaskFacade;
	
	@Before
	public void before() throws IOException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		mysqlTaskFacade = new MysqlTaskFacade();
		mysqlTaskFacade.setMysqlUtil(mysqlUtil);
	}
	

	@Test
	public void t() throws JSchException, IOException {
		mysqlTaskFacade.mysqlDump(session, box);
		
	}

}
