package com.go2wheel.mysqlbackup.sshcommon;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.JSchException;

public class TestCoreNumber extends SpringBaseFort {
	
	@Test
	public void tWin() throws SchedulerException, JSchException, IOException {
		clearDb();
		createSessionLocalHostWindows();
		deleteAllJobs();
		int core = SSHcommonUtil.coreNumber(server.getOs(), session);
		assertThat(core, greaterThan(1));
	}
	
	@Test
	public void tLinux() throws SchedulerException, JSchException, IOException {
		clearDb();
		createSession();
		deleteAllJobs();
		int core = SSHcommonUtil.coreNumber(server.getOs(), session);
		assertThat(core, greaterThan(1));
	}

}
