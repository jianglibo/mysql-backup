package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.installer.BorgInstaller;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;

public class TestBorgArchiveJob extends JobBaseFort {
	
	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgService borgService;
	
	@Autowired
	private BorgDownloadDbService borgDownloadDbService;
	
	@Autowired
	private BorgInstaller borgInstaller;
	
	private Software software;
	
	@Test
	public void testJobFunction() throws SchedulerException, IOException {
		clearDb();
		long jc = countJobs();
		assertThat(jc, equalTo(0L));
		createSession();
		createBorgDescription();
		createContext();
		deleteAllJobs();
		software = softwareDbService.findByName("BORG").get(0);
		
		borgInstaller.install(session, server, software, null);
		borgArchiveJob.execute(context);
		
		borgDownloadDbService.count();
		assertThat(borgDownloadDbService.count(), equalTo(1L));
	}
	
	@Test
	public void testSchedulerBorg() throws SchedulerException {
		clearDb();
		createServer();
		deleteAllJobs();
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
		.withArchiveCron(A_VALID_CRON_EXPRESSION).build();
		bd = borgDescriptionDbService.save(bd);
		assertThat(countJobs(), equalTo(1L)); // cause only set archiveCron.
		assertThat(countTriggers(), equalTo(1L)); // cause only set archiveCron.
		
		borgDescriptionDbService.delete(bd); // deleting borgdescription will delete job too.
		assertThat(countJobs(), equalTo(0L));
		assertThat(countTriggers(), equalTo(0L));
	}
	
	@Test
	public void testSchedulerBorg2() throws SchedulerException {
		clearDb();
		createServer();
		deleteAllJobs();
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
		.withArchiveCron(A_VALID_CRON_EXPRESSION)
		.withPruneCron(A_VALID_CRON_EXPRESSION)
		.build();
		bd = borgDescriptionDbService.save(bd);
		assertThat(countJobs(), equalTo(2L)); // cause only set archiveCron.
		assertThat(countTriggers(), equalTo(2L)); // cause only set archiveCron.
		
		borgDescriptionDbService.delete(bd); // deleting borgdescription will delete job too. 
		assertThat(countJobs(), equalTo(0L));
		
		assertThat(countTriggers(), equalTo(0L));
	}
	
	@Test
	public void testSchedulerBorgUpdate() throws SchedulerException {
		clearDb();
		long tc = countTriggers();
		assertThat(tc, equalTo(0L));
		createServer();
		deleteAllJobs();
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
		.withArchiveCron(A_VALID_CRON_EXPRESSION).build();
		bd = borgDescriptionDbService.save(bd);
		assertThat(countTriggers(), equalTo(1L));
		
		bd = borgDescriptionDbService.save(bd);
		assertThat(countTriggers(), equalTo(1L));
		
		bd.setArchiveCron(null);
		borgDescriptionDbService.save(bd);
		assertThat(countTriggers(), equalTo(0L));
	}
}
