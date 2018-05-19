package com.go2wheel.mysqlbackup.borg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

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
import com.go2wheel.mysqlbackup.value.FacadeResult;
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
	public void tRepoInit() throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");
			borgTaskFacade.install(session);
			FacadeResult<?> fr = borgTaskFacade.initRepo(session, "");
			assertFalse(fr.isExpected());
			assertThat(fr.getMessage(), equalTo("borg.repo.wrongpath"));
			fr = borgTaskFacade.initRepo(session, null);
			assertFalse(fr.isExpected());
			assertThat(fr.getMessage(), equalTo("common.file.exists"));
			
			fr = borgTaskFacade.initRepo(session, "/abc");
			assertTrue(fr.isExpected());
			
			fr = borgTaskFacade.initRepo(session, "/abc");
			assertFalse(fr.isExpected());
			assertThat(fr.getMessage(), equalTo("common.file.exists"));

			
			SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");
			
	}
	
	@Test
	public void testArchive() {
		borgTaskFacade.unInstall(session);
		FacadeResult<?> fr = borgTaskFacade.archive(session, box);
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo("common.application.notinstalled"));
		
		fr = borgTaskFacade.install(session);
		assertTrue(fr.isExpected());
		
		fr = borgTaskFacade.archive(session, box);
		assertTrue(fr.isExpected());
		
	}


	
	
	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.install(session);
		SSHcommonUtil.runRemoteCommand(session, String.format("rm -rvf %s", box.getBorgBackup().getRepo()));
		RemoteCommandResult rcr1 = borgTaskFacade.initRepo(session, box.getBorgBackup().getRepo()).getResult();
		assertThat(rcr1.getExitValue(), equalTo(0));
		FacadeResult<?> fr = borgTaskFacade.archive(session, box);
		assertTrue(fr.isExpected());

		borgTaskFacade.downloadRepo(session, box);
		
		for(int i = 0; i< 2; i++) {
			archive();
		}
		
		BorgListResult blr = borgTaskFacade.listArchives(session, box).getResult();
		assertThat(blr.getArchives().size(), equalTo(3));
		
		BorgPruneResult bpr = borgTaskFacade.pruneRepo(session, box).getResult();
		assertTrue(bpr.isSuccess());
		assertThat(bpr.prunedArchiveNumbers(), equalTo(2L));
		assertThat(bpr.keepedArchiveNumbers(), equalTo(1L));
		
		blr = borgTaskFacade.listArchives(session, box).getResult();
		assertThat(blr.getArchives().size(), equalTo(1));
		
		
		int c = SSHcommonUtil.countFiles(session, box.getBorgBackup().getRepo());
		assertThat(c, greaterThan(3));
	}
	
	@Test
	public void tArchiveNoPath() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.install(session);
		SSHcommonUtil.runRemoteCommand(session, String.format("rm -rvf %s", box.getBorgBackup().getRepo()));
		RemoteCommandResult rcr1 = borgTaskFacade.initRepo(session, box.getBorgBackup().getRepo()).getResult();
		assertThat(rcr1.getExitValue(), equalTo(0));
		box.getBorgBackup().setIncludes(new ArrayList<>());
		box.getBorgBackup().setExcludes(new ArrayList<>());
		FacadeResult<?> fr = borgTaskFacade.archive(session, box);
		assertFalse("result should fail.", fr.isExpected());
		assertThat(fr.getMessage(), equalTo("borg.archive.noincludes"));
	}
	
	private void archive() throws RunRemoteCommandException, InterruptedException {
		borgTaskFacade.archive(session, box);
		Thread.sleep(1000);
	}

}
