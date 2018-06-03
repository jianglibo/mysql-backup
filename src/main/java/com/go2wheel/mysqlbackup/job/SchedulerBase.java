package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class SchedulerBase {
	
	@Autowired
	protected Scheduler scheduler;
	
	@Autowired
	protected ServerService serverService;
	
	public void createTrigger(Server server, String cronExpression, Class<? extends Job> jobClass, JobKey jk, TriggerKey tk) throws SchedulerException, ParseException {
		if (!StringUtil.hasAnyNonBlankWord(cronExpression)) return;
		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(jobClass)
					.withIdentity(jk)
					.usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, server.getId())
					.storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(cronExpression);
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}
	
	protected void reschedule(Server server, String cronExpBefore, String cronExpAfter, Class<? extends Job> jobClass, JobKey jk, TriggerKey tk) throws SchedulerException, ParseException {
		if(cronExpBefore == null && cronExpAfter == null)return;
		if (cronExpBefore == null) { // cronExpAfter mustn't null.
			createTrigger(server, cronExpAfter, jobClass, jk, tk);
		} else if (cronExpAfter == null) { // cronExpBefore mustn't null;
			scheduler.unscheduleJob(tk);
		} else if (!cronExpBefore.equals(cronExpAfter)) {
			CronExpression ce = new CronExpression(cronExpAfter);
			Trigger trigger = newTrigger().withIdentity(tk)
						.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.rescheduleJob(tk, trigger);
		}
	}
	

}
