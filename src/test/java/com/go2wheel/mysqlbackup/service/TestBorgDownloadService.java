package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.job.BorgArchiveJob;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.ResultEnum;

public class TestBorgDownloadService extends JobBaseFort {

	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgService borgService;
	
	@Before
	public void b() {
		borgService.unInstall(session);
	}

	@Test
	public void tNoBorgInstalled() throws JobExecutionException {
		borgArchiveJob.execute(context);

		List<BorgDownload> downloads = borgDownloadService.getItemsInDays(server, 3);

		assertThat(downloads.size(), equalTo(1));
		assertThat(downloads.get(0).getResult(), equalTo(ResultEnum.FAIL));
		
		List<JobError> jes = jobErrorService.findAll();
		assertThat(jes.get(0).getMessageKey(), equalTo(CommonMessageKeys.APPLICATION_NOTINSTALLED));

	}
	
	@Test
	public void tBorgInstalled() throws JobExecutionException {
		borgService.install(session);
		borgArchiveJob.execute(context);
		List<BorgDownload> downloads = borgDownloadService.getItemsInDays(server, 3);

		assertThat(downloads.size(), equalTo(1));
		assertThat(downloads.get(0).getResult(), equalTo(ResultEnum.SUCCESS));

	}

}
