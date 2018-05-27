package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;

public class TestMysqlFlushLogJob extends SpringBaseFort {
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Autowired
	private MysqlFlushService mysqlFlushService;
	
	@Test
	public void t() throws JobExecutionException {
		JobDataMap jdm = new JobDataMap();
		jdm.put("host", HOST_DEFAULT);
		given(context.getMergedJobDataMap()).willReturn(jdm);
		mysqlFlushLogJob.execute(context);
		
		mysqlFlushService.count();
		assertThat(mysqlFlushService.count(), equalTo(1L));
		
	}

}
