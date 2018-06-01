package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.jcraft.jsch.JSchException;

public class TestMysqlLogin extends SpringBaseFort {

	@Test
	public void tLogin() throws IOException, JSchException, MysqlAccessDeniedException, MysqlNotStartedException {
		new MysqlInteractiveExpect<String>(session) {

			@Override
			protected String afterLogin() {
				return "";
			}
		}.start(server);
	}

}
