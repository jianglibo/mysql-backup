package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

import java.util.Arrays;
import java.util.Collections;

//import static org.quartz.JobKey.*;
//import static org.quartz.impl.matchers.KeyMatcher.*;
//import static org.quartz.impl.matchers.GroupMatcher.*;
//import static org.quartz.impl.matchers.AndMatcher.*;
//import static org.quartz.impl.matchers.OrMatcher.*;
//import static org.quartz.impl.matchers.EverythingMatcher.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.mockito.Mockito.*;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestScheduler {
	
	@Autowired
	private Scheduler scheduler;
	
	@MockBean
	private MyJobListener myJobListener;

	@Test
	public void testJobAndTriggers() throws SchedulerException, InterruptedException {
		
		when(myJobListener.getName()).thenReturn("myjl");
		assertNotNull(scheduler);
		List<String> grps = scheduler.getJobGroupNames();
		Collections.sort(grps);
		List<String> expected = Arrays.asList("group1", "MYSQL", SpringQrtzScheduler.GROUP_NAME);
		Collections.sort(expected);
		assertTrue(grps.contains("MYSQL") && grps.contains("FOR_TEST_GROUP") && grps.contains("group1"));
		
		
		scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
		
		Set<JobKey> jks = scheduler.getJobKeys(GroupMatcher.groupEquals("MYSQL"));
		jks = scheduler.getJobKeys(GroupMatcher.groupEquals("DEFAULT"));
		jks = scheduler.getJobKeys(GroupMatcher.groupEquals(SpringQrtzScheduler.GROUP_NAME));
		
		String jkname = jks.iterator().next().toString();
		assertThat(jkname, equalTo(SpringQrtzScheduler.GROUP_NAME + ".Qrtz_Job_Detail"));
		assertThat(jks.size(), equalTo(1));
		

//		List<String> triggergrps = scheduler.getTriggerGroupNames();
//		Collections.sort(triggergrps);
//		
//		assertTrue("trigger group should right.", triggergrps.contains("MYSQL") && triggergrps.contains(SpringQrtzScheduler.GROUP_NAME));
//		
//		Set<TriggerKey> trks = scheduler.getTriggerKeys(GroupMatcher.groupEquals("MYSQL"));
//		trks = scheduler.getTriggerKeys(GroupMatcher.groupEquals("DEFAULT"));
//		trks = scheduler.getTriggerKeys(GroupMatcher.groupEquals(SpringQrtzScheduler.GROUP_NAME));
//		
//		List<String> trknames = trks.stream().map(tk -> tk.toString()).collect(Collectors.toList());
//		assertThat(trks.size(), equalTo(3));
//		Collections.sort(trknames);
//		expected = Arrays.asList(SpringQrtzScheduler.GROUP_NAME + ".Qrtz_Trigger", SpringQrtzScheduler.GROUP_NAME + ".Qrtz_Trigger_1", SpringQrtzScheduler.GROUP_NAME + ".Qrtz_Trigger_2");
//		assertThat(trknames, equalTo(expected));
//		Thread.sleep(10000);
//		
//		verify(myJobListener, atLeastOnce()).jobToBeExecuted(any());
//		verify(myJobListener, atLeastOnce()).jobWasExecuted(any(), any());
		
	}
	
	public class MyJobListener implements JobListener {

	    private String name;
	    
	    public MyJobListener() {
	    	this.name = "myjl";
	    }

	    public MyJobListener(String name) {
	        this.name = name;
	    }

	    public String getName() {
	        return name;
	    }

	    public void jobToBeExecuted(JobExecutionContext context) {
	        System.out.println("a");
	    }

	    public void jobWasExecuted(JobExecutionContext context,
	            JobExecutionException jobException) {
	    	System.out.println("b");
	    }

	    public void jobExecutionVetoed(JobExecutionContext context) {
	    	System.out.println("c");
	    }
	}
}
