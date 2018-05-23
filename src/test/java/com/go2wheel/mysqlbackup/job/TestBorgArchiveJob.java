package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.service.BorgDownloadService;

public class TestBorgArchiveJob extends SpringBaseFort {
	
	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgDownloadService borgDownloadService;
	
	@Test
	public void t() throws JobExecutionException {
		borgArchiveJob.execute(context);
		borgDownloadService.count();
		assertThat(borgDownloadService.count(), equalTo(1L));
	}

}
