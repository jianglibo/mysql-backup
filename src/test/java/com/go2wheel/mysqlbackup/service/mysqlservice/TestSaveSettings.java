package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;

public class TestSaveSettings extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	@Test
	public void testSaveMysqlSettings() throws IOException, UnExpectedContentException, JSchException, SchedulerException {
		clearDb();
		installMysql();
		MycnfFileHolder mfh = mysqlService.getMysqlSettingsFromDisk(server);
		assertNotNull(mfh);
		assertThat(mfh.getMysqlVariables().getLogBinIndexNameOnly(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME + ".index"));
	}


}
