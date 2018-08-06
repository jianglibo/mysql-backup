package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;

public class TestEnableLogbin extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	
	@Test
	public void testEnableBinLog() throws UnExpectedContentException, JSchException, SchedulerException, IOException {
		clearDb();
		installMysql();
		FacadeResult<?> fr = mysqlService.disableLogbin(session, server);
		assertTrue(fr.isExpected());
		assertFalse(server.getMysqlInstance().getLogBinSetting().isEnabled());
		
		fr = mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
		assertTrue(fr.isExpected());
		assertTrue(server.getMysqlInstance().getLogBinSetting().isEnabled());
	}

}
