package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.installer.BorgInstaller;
import com.go2wheel.mysqlbackup.job.BorgArchiveJob;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Software;

public class TestBorgDownloadDbService extends JobBaseFort {

	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	
	@Autowired
	private BorgInstaller borgInstaller;
	
	private Software software;
	
	@Before
	public void b() {
		clearDb();
		createSession();
		createBorgDescription();
		createContext();
		software = softwareDbService.findByName("BORG").get(0);
		
		borgInstaller.install(session, server, software, null);
	}

	@Test(expected=UnExpectedContentException.class)
	public void tNoBorgInstalled() throws JobExecutionException {
		borgArchiveJob.execute(context);
	}
	
	@Test
	public void tBorgInstalled() throws JobExecutionException {
		software = softwareDbService.findByName("BORG").get(0);
		
		borgInstaller.install(session, server, software, null);
		borgArchiveJob.execute(context);
		List<BorgDownload> downloads = borgDownloadDbService.getItemsInDays(server, 3);

		assertThat(downloads.size(), equalTo(1));
	}

}
