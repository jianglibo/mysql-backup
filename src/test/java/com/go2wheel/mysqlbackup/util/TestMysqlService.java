package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.MysqlUtil.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.jcraft.jsch.JSchException;

public class TestMysqlService extends SpringBaseFort {

	@Autowired
	private MysqlService mysqlService;


	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(session, box);
		assertTrue(fr.isExpected());
	}
	
	@Test
	public void testMysqlFlush()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		FacadeResult<String> fr = mysqlService.mysqlFlushLogs(session, box);
		assertTrue(fr.isExpected());
		assertTrue(Files.exists(Paths.get(fr.getResult())));
	}

}
