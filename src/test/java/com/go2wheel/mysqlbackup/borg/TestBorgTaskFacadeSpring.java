package com.go2wheel.mysqlbackup.borg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.InstallationInfo;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestBorgTaskFacadeSpring {
	
	@Autowired
	private BorgTaskFacade borgTaskFacade;
	
	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.archive(null, null, null);
	}

}
