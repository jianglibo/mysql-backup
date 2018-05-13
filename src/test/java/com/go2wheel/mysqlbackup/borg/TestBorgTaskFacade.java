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

import com.go2wheel.mysqlbackup.borg.BorgService.InstallationInfo;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestBorgTaskFacade extends SshBaseFort {
	
	private BorgService borgTaskFacade;
	
	@Before
	public void b() throws IOException, RunRemoteCommandException {
		super.before();
		borgTaskFacade = new BorgService();
		borgTaskFacade.setAppSettings(appSettings);
		FileDownloader fd = new FileDownloader();
		fd.setAppSettings(appSettings);
		fd.post();
		borgTaskFacade.setFileDownloader(fd);
		InstallationInfo ii = borgTaskFacade.unInstall(session).getResult();
		assertFalse("borg should not installed now.", ii.isInstalled());
	}
	
	@After
	public void a() throws IOException, JSchException, RunRemoteCommandException {
		super.after();
	}
	
	
	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.install(session);
		SSHcommonUtil.runRemoteCommand(session, String.format("rm -rvf %s", box.getBorgBackup().getRepo()));
		RemoteCommandResult rcr = borgTaskFacade.initRepo(session, box.getBorgBackup().getRepo()).getResult();
		assertThat(rcr.getExitValue(), equalTo(0));
		rcr = borgTaskFacade.archive(session, box, box.getBorgBackup().getArchiveNamePrefix()).getResult();
		assertThat(rcr.getExitValue(), equalTo(0));

		borgTaskFacade.downloadRepo(session, box);
		assertThat(rcr.getExitValue(), equalTo(0));
		
		for(int i = 0; i< 9; i++) {
			archive();
		}
		
		BorgListResult blr = borgTaskFacade.listArchives(session, box).getResult();
		assertThat(blr.getArchives().size(), equalTo(10));
		
		BorgPruneResult bpr = borgTaskFacade.pruneRepo(session, box).getResult();
		assertTrue(bpr.isSuccess());
		assertThat(bpr.prunedArchiveNumbers(), equalTo(9L));
		assertThat(bpr.keepedArchiveNumbers(), equalTo(1L));
		
		blr = borgTaskFacade.listArchives(session, box).getResult();
		assertThat(blr.getArchives().size(), equalTo(1));
		
		
		int c = SSHcommonUtil.countFiles(session, box.getBorgBackup().getRepo());
		assertThat(c, greaterThan(3));
	}
	
	private void archive() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.archive(session, box, box.getBorgBackup().getArchiveNamePrefix());
		Thread.sleep(1000);
	}

}
