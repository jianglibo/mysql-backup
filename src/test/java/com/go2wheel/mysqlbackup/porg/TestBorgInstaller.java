package com.go2wheel.mysqlbackup.porg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.borg.BorgTaskFacade;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.value.InstallationInfo;
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
		borgInstaller.unInstall(session);
		super.after();
	}
	
	
	@Test
	public void tInstall() throws RunRemoteCommandException {
		InstallationInfo ii = borgInstaller.install(session);
		assertTrue(ii.isInstalled());
		ii = borgInstaller.unInstall(session);
		assertFalse(ii.isInstalled());
		
	}

}
