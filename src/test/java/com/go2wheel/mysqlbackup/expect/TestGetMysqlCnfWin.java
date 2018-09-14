package com.go2wheel.mysqlbackup.expect;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;

public class TestGetMysqlCnfWin extends SpringBaseFort {
	
	@Autowired
	private MysqlService mysqlService;
	
	@Value("${myapp.app.client-bin}")
	private String clientBin;
	
	@Test
	public void tGetMycnf() throws IOException, JSchException, SchedulerException, UnExpectedOutputException {
		createSessionLocalHostWindowsAfterClear();
		createMysqlIntance(server, "123456");
		server.getMysqlInstance().setClientBin(clientBin);
		
		FacadeResult<String> p = mysqlService.getMyCnfFile(session, server);
		
		assertTrue(Files.exists(Paths.get(p.getResult())));
	}

}
