package com.go2wheel.mysqlbackup.borg;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.borg.BorgTaskFacade;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.InstallationInfo;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestBorgInstaller extends SshBaseFort {
	
	private BorgTaskFacade borgInstaller;
	
	@Before
	public void b() throws IOException, RunRemoteCommandException {
		super.before();
		borgInstaller = new BorgTaskFacade();
		borgInstaller.setAppSettings(appSettings);
		InstallationInfo ii = borgInstaller.unInstall(session);
		assertFalse("borg should not installed now.", ii.isInstalled());
	}
	
	@After
	public void a() throws IOException, JSchException, RunRemoteCommandException {

		super.after();
	}
	
	
	@Test
	public void tArchive() throws RunRemoteCommandException {
		borgInstaller.install(session);
		borgInstaller.initRepo(session, box.getBorgBackup().getRepo());
		RemoteCommandResult rcr = borgInstaller.archive(session, box, "ARCHIVE-");
		rcr.printOutput();
		int c = SSHcommonUtil.countFiles(session, box.getBorgBackup().getRepo());
		assertThat(c, greaterThan(3));
	}

}
