package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.job.BorgArchiveJob;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.BorgDownload;

public class TestBorgDownloadDbService extends JobBaseFort {

	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgService borgService;
	
	@Before
	public void b() {
		clearDb();
		createSession();
		createBorgDescription();
		createContext();
		borgService.unInstall(session);
	}

	@Test(expected=UnExpectedContentException.class)
	public void tNoBorgInstalled() throws JobExecutionException {
		borgArchiveJob.execute(context);
	}
	
	@Test
	public void tBorgInstalled() throws JobExecutionException {
		borgService.install(session);
		borgArchiveJob.execute(context);
		List<BorgDownload> downloads = borgDownloadDbService.getItemsInDays(server, 3);

		assertThat(downloads.size(), equalTo(1));
	}

}
