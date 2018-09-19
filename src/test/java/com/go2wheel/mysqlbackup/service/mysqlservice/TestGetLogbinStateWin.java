package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestGetLogbinStateWin extends MysqlServiceTbase {
	
//	GRANT LOCK TABLES, SELECT ON *.* TO 'BACKUPUSER'@'%' IDENTIFIED BY 'PASSWORD';
	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc;
	
	
	@Value("${myapp.app.client-bin}")
	private String clientBin;
	
	
	@Test
	public void testEnableBinLog() throws UnExpectedOutputException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, CommandNotFoundException {
		createSessionLocalHostWindowsAfterClear();
		createMysqlIntance();
		
		server.getMysqlInstance().setClientBin(clientBin);
		
		sdc.setHost(server.getHost());
		//very slow.
		MysqlVariables v = mysqlUtil.getLogbinStateWin(session, server, server.getMysqlInstance());
		assertThat(v.getMap().size(), greaterThan(1));
		
		String s = "& e:\\wamp64\\bin\\mysql\\mysql5.7.21\\bin\\mysql.exe -uroot -p123456 -e 'show variables' | Foreach-Object {$_.trim()} |Where-Object {$_} |Where-Object {@('log_bin','log_bin_basename','log_bin_index','innodb_version','protocol_version','version','version_comment','version_compile_machine','version_compile_os','datadir') -contains ($_ -split '\\s+')[0] } | ForEach-Object -Begin {'---start---'} -Process {',,,' + $_ + ',,,'} -End {'---end---'}";
		
		RemoteCommandResult r = SSHcommonUtil.runRemoteCommand(session, s);
		
		assertNotNull(r);
		
	}

}
