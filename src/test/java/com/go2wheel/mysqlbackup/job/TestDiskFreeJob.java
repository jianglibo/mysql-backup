package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.service.DiskfreeDbService;

public class TestDiskFreeJob extends JobBaseFort {
	
	@Autowired
	private StorageStateJob diskfreeJob;
	
	@Autowired
	private DiskfreeDbService diskfreeDbService;
	
	@Test
	public void t() throws JobExecutionException {
		JobDataMap jdm = new JobDataMap();
		jdm.put("host", HOST_DEFAULT);
		given(context.getMergedJobDataMap()).willReturn(jdm);
		diskfreeJob.execute(context);
		
		diskfreeDbService.count();
		assertThat(diskfreeDbService.count(), greaterThan(3L));
		
	}

}
