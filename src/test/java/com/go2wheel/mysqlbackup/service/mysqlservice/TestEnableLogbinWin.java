package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.jcraft.jsch.JSchException;

public class TestEnableLogbinWin extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	@Value("${myapp.app.client-bin}")
	private String clientBin;
	
	
	@Test
	public void testEnableBinLog() throws UnExpectedContentException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		createSessionLocalHostWindowsAfterClear();
		sdc.setHost(server.getHost());
		createMysqlIntance(server, "123456");
		
		server.getMysqlInstance().setClientBin(clientBin);
		
		FacadeResult<?> fr = mysqlService.disableLogbin(session, server);
		assertTrue(fr.isExpected());
		assertFalse(server.getMysqlInstance().getLogBinSetting().isEnabled());
		
		fr = mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
		assertTrue(fr.isExpected());
		assertTrue(server.getMysqlInstance().getLogBinSetting().isEnabled());
		
		Path mycnf = settingsIndb.getLocalMysqlDir(server).resolve(settingsIndb.getString("mysql.filenames.mycnf", "mycnf.yml"));
		
		assertTrue(Files.exists(mycnf));
		
		MycnfFileHolder mf = mysqlService.getMysqlSettingsFromDisk(mycnf);
		assertThat(mf.getMysqlVariables().getDataDirEndNoSlash(), equalTo("/var/lib/mysql"));
		assertThat(mf.getMysqlVariables().getDataDirEndWithSlash(), equalTo("/var/lib/mysql/"));
		
		MysqlInstallInfo mi = mysqlUtil.getInstallInfo(session, server);
		
		assertThat(mi.getVariables().get(MysqlVariables.DATA_DIR), equalTo("/var/lib/mysql/"));
	}

}
