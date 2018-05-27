package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;
import static org.quartz.impl.matchers.GroupMatcher.anyGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

//import static org.quartz.JobKey.*;
//import static org.quartz.impl.matchers.KeyMatcher.*;
//import static org.quartz.impl.matchers.GroupMatcher.*;
//import static org.quartz.impl.matchers.AndMatcher.*;
//import static org.quartz.impl.matchers.OrMatcher.*;
//import static org.quartz.impl.matchers.EverythingMatcher.*;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.UtilForTe;

@Import(com.go2wheel.mysqlbackup.job.TestSpringQrtzScheduler.SpringQrtzScheduler.class)
public class TestSpringQrtzScheduler extends SpringBaseFort  {
	
	@Autowired
	private Scheduler scheduler;
	
	@MockBean
	private MyJobListener myJobListener;
	
	@After
	public void after() throws SchedulerException {
		UtilForTe.deleteAllJobs(scheduler);
	}


	@Test
	public void testJobAndTriggers() throws SchedulerException, InterruptedException {
		
//		when(myJobListener.getName()).thenReturn("myjl");
//		assertNotNull(scheduler);
//		List<String> grps = scheduler.getJobGroupNames();
//		Collections.sort(grps);
//		List<String> expected = Arrays.asList("group1", "MYSQL", SpringQrtzScheduler.GROUP_NAME);
//		Collections.sort(expected);
//		assertTrue(grps.contains("FOR_TEST_GROUP") && grps.contains("group1"));
//		
//		
//		scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
//		
//		Set<JobKey> jks = scheduler.getJobKeys(GroupMatcher.groupEquals("MYSQL"));
//		jks = scheduler.getJobKeys(GroupMatcher.groupEquals("DEFAULT"));
//		jks = scheduler.getJobKeys(GroupMatcher.groupEquals(SpringQrtzScheduler.GROUP_NAME));
//		
//		String jkname = jks.iterator().next().toString();
//		assertThat(jkname, equalTo(SpringQrtzScheduler.GROUP_NAME + ".Qrtz_Job_Detail"));
//		assertThat(jks.size(), equalTo(1));
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
	
	@TestConfiguration
	public static class SpringQrtzScheduler {

	    Logger logger = LoggerFactory.getLogger(getClass());
	    
	    public static final String GROUP_NAME = "FOR_TEST_GROUP";

	    @Autowired
	    private ApplicationContext applicationContext;
	    
	    @PostConstruct
	    public void init() {
	        logger.info("Hello world from Spring...");
	    }


	    @Bean
	    public JobDetailFactoryBean sampleJobDetail() {
	        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
	        jobDetailFactory.setJobClass(SampleJob.class);
	        jobDetailFactory.setName("Qrtz_Job_Detail");
	        jobDetailFactory.setGroup(GROUP_NAME);
	        jobDetailFactory.setDescription("Invoke Sample Job service...");
	        jobDetailFactory.setDurability(true);
	        JobDataMap jobDataMap = new JobDataMap();
	        jobDataMap.put("date", new Date());
	        jobDetailFactory.setJobDataMap(jobDataMap);
	        return jobDetailFactory;
	    }
	    
	    @Bean
	    public DynamicTriggers dynamicTriggers() {
	    	return new DynamicTriggers();
	    }
	    
	    @Bean
	    public SampleJobService sampleJobService() {
	    	return new SampleJobService();
	    }
	    

	    @Bean
	    public SimpleTriggerFactoryBean sampleJobTrigger() {
	        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
	        trigger.setJobDetail(sampleJobDetail().getObject());

	        int frequencyInSec = 10;
	        logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);

	        trigger.setRepeatInterval(frequencyInSec * 1000);
	        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
	        trigger.setName("Qrtz_Trigger");
	        trigger.setGroup(GROUP_NAME);
	        return trigger;
	    }
	    
	    
	    public static class DynamicTriggers {
	    	
	    	Logger logger = LoggerFactory.getLogger(getClass());
	        public static final String GROUP_NAME = "FOR_TEST_GROUP";
	        
	        @Autowired
	        private Scheduler scheduler;

	    	
	    	@PostConstruct
	    	public void post() throws SchedulerException {
	    		scheduler.getJobKeys(anyGroup()).stream().forEach(jk -> {
	    			System.out.println(jk);
	    		});
	    		
	    		JobKey jk = jobKey("job1", "group1");
	    		
	    		JobDetail job = scheduler.getJobDetail(jk);
	    		if (job != null) {
	    			scheduler.deleteJob(jk);	
	    		}
	    		
	    		
	    		JobDetail job1 = newJob(PrintPropsJob.class)
	    			    .withIdentity(jk)
	    			    .usingJobData("someProp", "someValue")
	    			    .storeDurably()
	    			    .build();
	    		
	    		scheduler.addJob(job1, false);

	    		
	    		Trigger trigger = newTrigger()
	    			    .withIdentity("trigger1", "group1")
	    			    .startNow()
	    			    .forJob(jk)
	    			    .build();
	    			// Schedule the trigger
	    		scheduler.scheduleJob(trigger);
	    	}
	    }
	    
	    
//	    @Bean
//	    public JobDetailFactoryBean sampleJobDetail1() {
//	        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
//	        jobDetailFactory.setJobClass(SampleJob.class);
//	        jobDetailFactory.setName("Qrtz_Job_Detail");
//	        jobDetailFactory.setDescription("Invoke Sample Job service...");
//	        jobDetailFactory.setDurability(true);
//	        return jobDetailFactory;
//	    }

	//    
//	    @Bean
//	    public CronTriggerFactoryBean sampleJobTrigger2() {
//	    	CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
//	        trigger.setJobDetail(sampleJobDetail().getObject());
//	        trigger.setCronExpression("0 0 12 * * ?");
//	        trigger.setName("Qrtz_Trigger_2");
//	        trigger.setGroup(GROUP_NAME);
//	        return trigger;
//	    }
	//    
//	    @Bean
//	    public CronTriggerFactoryBean sampleJobTrigger3() {
//	    	CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
//	        trigger.setJobDetail(sampleJobDetail().getObject());
//	        trigger.setCronExpression("0 0 12 * * ?");
//	        trigger.setName("Qrtz_Trigger_2");
//	        return trigger;
//	    }

//	    @Bean
//	    public SimpleTriggerFactoryBean sampleJobTrigger1() {
//	        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
//	        trigger.setJobDetail(sampleJobDetail().getObject());
	//
//	        int frequencyInSec = 10;
//	        logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);
	//
//	        trigger.setRepeatInterval(frequencyInSec * 1000);
//	        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
//	        trigger.setName("Qrtz_Trigger_1");
//	        trigger.setGroup(GROUP_NAME);
//	        return trigger;
//	    }
	    
		@Bean
		public SampleJob sampleJob() {
			return new SampleJob();
		}
		
		
		public static class SampleJob implements Job {

		    Logger logger = LoggerFactory.getLogger(getClass());

		    @Autowired
		    private SampleJobService jobService;
		    
		    private Date date;

		    public void execute(JobExecutionContext context) throws JobExecutionException {

		        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

		        jobService.executeSampleJob();
		        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		        
		        JobDataMap jobDataMap = context.getMergedJobDataMap();
		        Date date = (Date) jobDataMap.get("date");

		        logger.info("Next job scheduled @ {}", context.getNextFireTime());
		    }

			public Date getDate() {
				return date;
			}

			public void setDate(Date date) {
				this.date = date;
			}
		}
		
		public static class SampleJobService {
		    private Logger logger = LoggerFactory.getLogger(getClass());

		    public void executeSampleJob() {

		        logger.info("The sample job has begun...");
		        try {
		            Thread.sleep(5000);
		        } catch (InterruptedException e) {
		            logger.error("Error while executing sample job", e);
		        } finally {
		            logger.info("Sample job has finished...");
		        }
		    }
		}
	}
}
