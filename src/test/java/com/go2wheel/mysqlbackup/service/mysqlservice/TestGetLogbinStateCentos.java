package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.jcraft.jsch.JSchException;

public class TestGetLogbinStateCentos extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	
	@Test
	public void testEnableBinLog() throws UnExpectedOutputException, JSchException, SchedulerException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, CommandNotFoundException {
		clearDb();
		installMysql();
		sdc.setHost(server.getHost());
		MysqlVariables v = mysqlUtil.getLogbinStateCentos(session, server, server.getMysqlInstance());
		assertThat(v.getMap().size(), greaterThan(1));
	}

}
