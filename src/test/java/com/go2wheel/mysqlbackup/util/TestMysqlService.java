package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.jcraft.jsch.JSchException;

public class TestMysqlService extends SpringBaseFort {

	@Autowired
	private MysqlService mysqlService;


	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(session, server);
		assertTrue(fr.isExpected());
	}
	
	@Test
	public void testMysqlFlush()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		FacadeResult<String> fr = mysqlService.mysqlFlushLogs(session, server);
		assertTrue(fr.isExpected());
		assertTrue(Files.exists(Paths.get(fr.getResult())));
	}

}
