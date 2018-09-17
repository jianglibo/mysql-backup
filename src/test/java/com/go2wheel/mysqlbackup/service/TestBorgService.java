package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.RemoteTfolder;
import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.installer.BorgInstallInfo;
import com.go2wheel.mysqlbackup.installer.BorgInstaller;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestBorgService extends SpringBaseFort {

	@Autowired
	private BorgService borgService;
	
	@Autowired
	private BorgInstaller borgInstaller;
	
	private String extractFolder = "/opt/borg-extract";
	
    @Rule
    public RemoteTfolder rtfoler = new RemoteTfolder("/opt/borgrepos");
	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	
	private Software software;

	@Before
	public void b() throws IOException, RunRemoteCommandException, SchedulerException, JSchException {
		clearDb();
		createSession();
		createBorgDescription();
		deleteAllJobs();
		borgInstaller.syncToDb();
		List<Software> sfs = softwareDbService.findByName("BORG");
		software = sfs.get(0);
		BorgInstallInfo ii = (BorgInstallInfo) borgInstaller.unInstall(session, server, software).getResult();
		assertFalse(ii.isInstalled());
	}


	/**
	 * If repo path already exist, initializing on that path will fail.
	 * 
	 * @throws RunRemoteCommandException
	 * @throws CommandNotFoundException 
	 * @throws IOException 
	 * @throws JSchException 
	 */
	@Test
	public void tRepoInit() throws RunRemoteCommandException, CommandNotFoundException, JSchException, IOException {
		sdc.setHost(HOST_DEFAULT_GET);
		SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");
		borgInstaller.install(session, server, software, null);
		
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
		assertThat(fr.getMessage(), equalTo(CommonMessageKeys.OBJECT_ALREADY_EXISTS));

		SSHcommonUtil.runRemoteCommand(session, "rm -rvf /abc");

	}
	
	@Test
	public void testPlayBackSync() throws JSchException, IOException, CommandNotFoundException, NoSuchAlgorithmException {
		sdc.setHost(HOST_DEFAULT_GET);
		borgInstaller.install(session, server, software, null);
		borgService.initRepo(session, server.getBorgDescription().getRepo());
		borgService.archive(session, server);
		borgService.downloadRepo(session, server);
		
		String sourceRepo = server.getBorgDescription().getRepo();
		Server serverTarget = createServer("192.168.33.111");
		Session sessionTarget = sshSessionFactory.getConnectedSession(serverTarget).getResult();
		borgInstaller.install(sessionTarget, serverTarget, software, null);
		rtfoler.setSession(sessionTarget);
		PlayBack pb = new PlayBack();
		pb.setPlayWhat(PlayBack.PLAY_BORG);
		pb.setSourceServerId(server.getId());
		pb.setTargetServerId(serverTarget.getId());
		pb = playBackDbService.save(pb);
		
		borgService.playbackSync(pb, PathUtil.getFileName(settingsIndb.getCurrentRepoDir(server).toAbsolutePath().toString()));
		// target repo is same with source.
		List<String> sourcetList = borgService.listArchives(session, sourceRepo).getResult().getArchiveNames();
		List<String> targetList = borgService.listArchives(sessionTarget, sourceRepo).getResult().getArchiveNames();
		assertThat(sourcetList.size(), equalTo(targetList.size()));
		FacadeResult<List<LinuxLsl>> fr = borgService.extract(sessionTarget,server, serverTarget, /*sourceRepo,*/ targetList.get(0), extractFolder);
		assertThat(fr.getResult().size(), equalTo(server.getBorgDescription().getIncludes().size()));
	}


	@Test(expected = CommandNotFoundException.class)
	public void testArchive() throws CommandNotFoundException, JSchException, IOException {
		sdc.setHost(HOST_DEFAULT_GET);
		FacadeResult<?> fr = borgInstaller.unInstall(session, server, software);
		fr = borgService.archive(session, server);
		assertFalse(fr.isExpected());
		assertThat(fr.getMessage(), equalTo(CommonMessageKeys.APPLICATION_NOTINSTALLED));

		fr = borgInstaller.install(session, server, software, null);
		assertTrue(fr.isExpected());

		fr = borgService.archive(session, server);
		assertTrue(fr.isExpected());

	}

	@Test
	public void tArchive() throws RunRemoteCommandException, InterruptedException, CommandNotFoundException, JSchException, NoSuchAlgorithmException, IOException {
		sdc.setHost(HOST_DEFAULT_GET);
		borgInstaller.install(session, server, software, null);
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
	public void tArchiveNoPath() throws RunRemoteCommandException, InterruptedException, CommandNotFoundException, JSchException, IOException {
		sdc.setHost(HOST_DEFAULT_GET);
		borgInstaller.install(session, server, software, null);
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

	private void archive() throws RunRemoteCommandException, InterruptedException, CommandNotFoundException, JSchException, IOException {
		borgService.archive(session, server);
		Thread.sleep(1000);
	}

}
