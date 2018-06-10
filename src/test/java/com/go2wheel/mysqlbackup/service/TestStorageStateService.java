package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.StorageState;

public class TestStorageStateService extends JobBaseFort {

	@Autowired
	private StorageStateService storageStateService;

	@Test
	public void t() throws SchedulerException {
		createServer();
		deleteAllJobs();
		createSession();
		List<StorageState> sss = storageStateService.getLinuxStorageState(server, session);
		
		assertThat(sss.size(), greaterThan(2));
		
		List<StorageState> sssdb = storageStateDbService.findAll();
		
		assertThat(sss.size(), equalTo(sssdb.size()));

	}

}
