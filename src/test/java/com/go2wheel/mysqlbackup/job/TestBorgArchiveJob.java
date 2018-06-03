package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.service.BorgDownloadService;

public class TestBorgArchiveJob extends JobBaseFort {
	
	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgDownloadService borgDownloadService;
	
	@Test
	public void testJobFunction() throws SchedulerException {
		long jc = countJobs();
		assertThat(jc, equalTo(0L));
		createServer();
		createBorgDescription();
		createContext();
		deleteAllJobs();
		borgArchiveJob.execute(context);
		borgDownloadService.count();
		assertThat(borgDownloadService.count(), equalTo(1L));
	}
	
	@Test
	public void testSchedulerBorg() throws SchedulerException {
		createServer();
		deleteAllJobs();
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
		.withArchiveCron(A_VALID_CRON_EXPRESSION).build();
		bd = borgDescriptionService.save(bd);
		assertThat(countJobs(), equalTo(1L));
		assertThat(countTriggers(), equalTo(1L));
		
		borgDescriptionService.delete(bd);
		assertThat(countJobs(), equalTo(1L));
		assertThat(countTriggers(), equalTo(0L));
	}
	
	@Test
	public void testSchedulerBorgUpdate() throws SchedulerException {
		long tc = countTriggers();
		assertThat(tc, equalTo(0L));
		createServer();
		deleteAllJobs();
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
		.withArchiveCron(A_VALID_CRON_EXPRESSION).build();
		bd = borgDescriptionService.save(bd);
		assertThat(countTriggers(), equalTo(1L));
		
		bd = borgDescriptionService.save(bd);
		assertThat(countTriggers(), equalTo(1L));
		
		bd.setArchiveCron(null);
		borgDescriptionService.save(bd);
		assertThat(countTriggers(), equalTo(0L));
	}
}
