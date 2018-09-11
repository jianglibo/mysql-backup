package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyBaseT;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.jcraft.jsch.JSchException;

public class TestRobocopyFullbackJob extends RobocopyBaseT {
	
	@MockBean
	protected JobExecutionContext context;
	
	@Autowired
	private RobocopyLocalRepoBackupJob robocopyLocalbackupJob;
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
    
	@Test
	public void tEmptyItems() throws SchedulerException, JSchException, IOException {
		createSessionLocalHostWindowsAfterClear();
		assertThat(countJobs(), equalTo(0L)); // new add mysqlinstance job.
		
		deleteRepo(server);
		RobocopyDescription rd = grpd(repofolder, srcfolder);
		
		rd.setServerId(server.getId());
		rd.setInvokeCron(A_VALID_CRON_EXPRESSION);
		rd = robocopyDescriptionDbService.save(rd);
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, rd.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		try {
			robocopyLocalbackupJob.execute(context);
		} catch (Exception e) {
			assertTrue(e instanceof ExceptionWrapper);
			ExceptionWrapper we = (ExceptionWrapper) e;
			assertTrue(we.getException() instanceof UnExpectedInputException);
		}
	}
    
	
	@Test
	public void tEmptySrcFolder() throws SchedulerException, JSchException, IOException {
		createSessionLocalHostWindowsAfterClear();
		assertThat(countJobs(), equalTo(0L)); // new add mysqlinstance job.
		deleteRepo(server);
		
		RobocopyDescription rd = grpd(repofolder, srcfolder);
		List<RobocopyItem> items = rd.getRobocopyItems();
		
		rd.setServerId(server.getId());
		rd.setInvokeCron(A_VALID_CRON_EXPRESSION);
		rd = robocopyDescriptionDbService.save(rd);
		
		RobocopyDescription frd = rd;
		items.stream().forEach(it -> {
			it.setDescriptionId(frd.getId());
			robocopyItemDbService.save(it);
		});
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, rd.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		robocopyLocalbackupJob.execute(context);
		
		List<Path> files = Files.list(settingsIndb.getCurrentRepoDir(server)).collect(Collectors.toList());
		
		assertThat(files.size(), equalTo(0)); //no increamental archive download.
		
	}
	
	private void deleteRepo(Server server) throws IOException {
		Files.list(settingsIndb.getReposDir(server)).forEach(d -> {
			try {
				FileUtil.deleteFolder(d, false);
			} catch (IOException e) {
			}
		});
	}
	
	@Test
	public void tNormalSrcFolder() throws SchedulerException, JSchException, IOException {
		createSessionLocalHostWindowsAfterClear();
		assertThat(countJobs(), equalTo(0L)); // new add mysqlinstance job.
		deleteRepo(server);
		RobocopyDescription rd = grpd(repofolder, srcfolder);
		List<RobocopyItem> items = rd.getRobocopyItems();
		
		rd.setServerId(server.getId());
		rd.setInvokeCron(A_VALID_CRON_EXPRESSION);
		rd = robocopyDescriptionDbService.save(rd);
		
		RobocopyDescription frd = rd;
		items.stream().forEach(it -> {
			it.setDescriptionId(frd.getId());
			robocopyItemDbService.save(it);
		});
		
		createDemoSrc(srcfolder);
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, rd.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		assertThat(countJobs(), equalTo(1L)); // new add mysqlinstance job.
		deleteAllJobs();
		
		robocopyLocalbackupJob.execute(context);
		
		List<Path> files = Files.list(settingsIndb.getReposDir(server)).collect(Collectors.toList());
		
		assertThat(files.size(), equalTo(2));
		
		// execute again. no file changed.
		robocopyLocalbackupJob.execute(context);
		files = Files.list(settingsIndb.getReposDir(server)).collect(Collectors.toList());
		assertThat(files.size(), equalTo(3));
		
		// add a new file.
		createALocalFile(srcfolder.getRoot().toPath().resolve("xx/axk.txt"), "abc");
		robocopyLocalbackupJob.execute(context);
		files = Files.list(settingsIndb.getReposDir(server)).collect(Collectors.toList());
		assertThat(files.size(), equalTo(4));

	}
	
	protected long countJobs() throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream().count();
	}
}
