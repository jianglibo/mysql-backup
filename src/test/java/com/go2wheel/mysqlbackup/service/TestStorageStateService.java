package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.jcraft.jsch.JSchException;

public class TestStorageStateService extends JobBaseFort {

	@Autowired
	private StorageStateService storageStateService;

	@Test
	public void t() throws SchedulerException, IOException, JSchException {
		clearDb();
		createServer();
		deleteAllJobs();
		createSession();
		List<StorageState> sss = storageStateService.getStorageState(server, session);
		
		assertThat(sss.size(), greaterThan(2));
		
		List<StorageState> sssdb = storageStateDbService.findAll();
		
		assertThat(sss.size(), equalTo(sssdb.size()));

	}

}
