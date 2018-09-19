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
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;

public class TestEnableLogbinWin extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	@Value("${myapp.app.client-bin}")
	private String clientBin;
	
	
	@Test
	public void testEnableBinLog() throws UnExpectedOutputException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, RunRemoteCommandException, ScpException, CommandNotFoundException {
		createSessionLocalHostWindowsAfterClear();
		sdc.setHost(server.getHost());
		createMysqlIntance(server, "123456");
		
		server.getMysqlInstance().setClientBin(clientBin);
		server.getMysqlInstance().setRestartCmd("Restart-Service wampmysqld64");
		
		mysqlService.disableLogbin(session, server);
//		assertTrue(fr.isExpected());
		assertFalse(server.getMysqlInstance().getLogBinSetting().isEnabled());
		
		mysqlService.enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
//		assertTrue(fr.isExpected());
		assertTrue(server.getMysqlInstance().getLogBinSetting().isEnabled());
		
		Path mycnf = settingsIndb.getLocalMysqlDir(server).resolve(settingsIndb.getString("mysql.filenames.mycnf", "mycnf.yml"));
		
		assertTrue(Files.exists(mycnf));
		
		MycnfFileHolder mf = mysqlService.getMysqlSettingsFromDisk(mycnf);
		assertThat(mf.getMysqlVariables().getDataDirEndNoPathSeparator(), equalTo("e:\\wamp64\\bin\\mysql\\mysql5.7.21\\data"));
		assertThat(mf.getMysqlVariables().getDataDirEndWithPathSeparator(), equalTo("e:\\wamp64\\bin\\mysql\\mysql5.7.21\\data\\"));
		
//		MysqlInstallInfo mi = mysqlUtil.getInstallInfo(session, server);
//		assertThat(mi.getVariables().get(MysqlVariables.DATA_DIR), equalTo("e:\\wamp64\\bin\\mysql\\mysql5.7.21\\data\\"));
	}

}
