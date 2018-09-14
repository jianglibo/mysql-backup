package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.jcraft.jsch.JSchException;

public class TestMysqlPasswordReadyExpect  extends SpringBaseFort {
	
	@Value("${myapp.app.client-bin}")
	private String clientBin;
	
	
	@Test
	public void tGetMycnf() throws IOException, JSchException, SchedulerException, UnExpectedOutputException, MysqlAccessDeniedException {
		createSessionLocalHostWindowsAfterClear();
		createMysqlIntance(server, "123456");
		server.getMysqlInstance().setClientBin(clientBin);
		
		String[] ss = new String[] {"innodb_version", "protocol_version", "version", "version_comment", "version_compile_machine", "version_compile_os"};
		
		Map<String, String> mp = new MysqlVariablesExpectWin(session, server, ss).start();
		
		assertThat(ss.length, equalTo(mp.size()));
		
		for(String key: ss) {
			assertTrue(mp.containsKey(key));
		}
		
		
	}
		
	

}
