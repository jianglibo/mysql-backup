package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.go2wheel.mysqlbackup.SpringBaseFort;

public class JobBaseFort extends SpringBaseFort {
	
	@MockBean
	protected JobExecutionContext context;
	
	@Before
	public void jbafter() throws SchedulerException {
		deleteAllJobs();
	}
	
	protected void assertJc(long before, int delta) throws SchedulerException {
		long now = countJobs();
		assertThat(now, equalTo(before + delta)); //create server will cause create uptime and diskfree jobs.
	}
	
	protected void assertTc(long before, int delta) throws SchedulerException {
		long now = countTriggers();
		assertThat(now, equalTo(before + delta)); //create server will cause create uptime and diskfree jobs.
	}
	
	protected long countJobs() throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream().count();
	}
	
	protected long countTriggers() throws SchedulerException {
		return scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup()).stream().count();
	}
	
	protected void printJobs() throws SchedulerException {
		scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream().forEach(System.out::println);	
	}
	
	@Test
	public void tplaceholder() {
		assertTrue(true);
	}
	
	

	
	protected void createContext() {
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, server.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
	}
}

