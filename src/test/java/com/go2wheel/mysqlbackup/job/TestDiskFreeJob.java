package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.service.DiskfreeService;

public class TestDiskFreeJob extends SpringBaseFort {
	
	@Autowired
	private DiskfreeJob diskfreeJob;
	
	@Autowired
	private DiskfreeService diskfreeService;
	
	@MockBean
	private JobExecutionContext context;
	
	@Test
	public void t() throws JobExecutionException {
		JobDataMap jdm = new JobDataMap();
		jdm.put("host", HOST_DEFAULT);
		given(context.getMergedJobDataMap()).willReturn(jdm);
		diskfreeJob.execute(context);
		
		diskfreeService.count();
		assertThat(diskfreeService.count(), greaterThan(3L));
		
	}

}
