package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.jcraft.jsch.JSchException;

public class TestMysqlLogin extends SpringBaseFort {

	@Test
	public void tLogin() throws IOException, JSchException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		clearDb();
		createSession();
		createMysqlIntance();
		new MysqlInteractiveExpect<String>(session) {
			@Override
			protected String afterLogin() {
				return "";
			}
		}.start(server);
	}

}
