package com.go2wheel.mysqlbackup.job;

import javax.annotation.PostConstruct;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.impl.matchers.GroupMatcher.*;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class DynamicTriggers {
	
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
