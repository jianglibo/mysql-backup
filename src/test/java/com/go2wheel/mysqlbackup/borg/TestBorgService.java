package com.go2wheel.mysqlbackup.borg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.borg.BorgService.InstallationInfo;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class TestBorgService extends SpringBaseFort {

	@Autowired
	private BorgService borgService;

	@Before
	public void b() throws IOException, RunRemoteCommandException, SchedulerException {
		InstallationInfo ii = borgService.unInstall(session).getResult();
		assertFalse(ii.isInstalled());
	}


	@Test
	public void tRepoInit() throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");
		borgService.install(session);
		
		FacadeResult<?> fr = borgService.initRepo(session, "");
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo(CommonMessageKeys.MALFORMED_VALUE));
		
		fr = borgService.initRepo(session, null);
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo(CommonMessageKeys.MALFORMED_VALUE));

		fr = borgService.initRepo(session, "/abc");
		assertTrue(fr.isExpected());

		fr = borgService.initRepo(session, "/abc");
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo(CommonMessageKeys.FILE_EXISTS));

		SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");

	}

	@Test
	public void testArchive() {
		borgService.unInstall(session);
		FacadeResult<?> fr = borgService.archive(session, server);
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo("common.application.notinstalled"));

		fr = borgService.install(session);
		assertTrue(fr.isExpected());

		fr = borgService.archive(session, server);
		assertTrue(fr.isExpected());

	}

	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException {
		borgService.install(session);
		SSHcommonUtil.runRemoteCommand(session, String.format("rm -rvf %s", server.getBorgDescription().getRepo()));
		RemoteCommandResult rcr1 = borgService.initRepo(session, server.getBorgDescription().getRepo()).getResult();
		assertThat(rcr1.getExitValue(), equalTo(0));
		FacadeResult<?> fr = borgService.archive(session, server);
		assertTrue(fr.isExpected());

		borgService.downloadRepo(session, server);

		for (int i = 0; i < 2; i++) {
			archive();
		}

		BorgListResult blr = borgService.listArchives(session, server).getResult();
		assertThat(blr.getArchives().size(), equalTo(3));

		BorgPruneResult bpr = borgService.pruneRepo(session, server).getResult();
		assertTrue(bpr.isSuccess());
		assertThat(bpr.prunedArchiveNumbers(), equalTo(2L));
		assertThat(bpr.keepedArchiveNumbers(), equalTo(1L));

		blr = borgService.listArchives(session, server).getResult();
		assertThat(blr.getArchives().size(), equalTo(1));

		int c = SSHcommonUtil.countFiles(session, server.getBorgDescription().getRepo());
		assertThat(c, greaterThan(3));
	}

	@Test
	public void tArchiveNoPath() throws RunRemoteCommandException, InterruptedException {
		borgService.install(session);
		BorgDescription bd = server.getBorgDescription();
		SSHcommonUtil.runRemoteCommand(session, String.format("rm -rvf %s", bd.getRepo()));
		RemoteCommandResult rcr1 = borgService.initRepo(session, bd.getRepo()).getResult();
		assertThat(rcr1.getExitValue(), equalTo(0));
		bd.setIncludes(new ArrayList<>());
		bd.setExcludes(new ArrayList<>());
		FacadeResult<?> fr = borgService.archive(session, server);
		assertFalse("result should fail.", fr.isExpected());
		assertThat(fr.getMessage(), equalTo("borg.archive.noincludes"));
	}

	private void archive() throws RunRemoteCommandException, InterruptedException {
		borgService.archive(session, server);
		Thread.sleep(1000);
	}

}
