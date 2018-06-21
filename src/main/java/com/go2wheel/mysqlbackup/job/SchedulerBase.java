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

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class SchedulerBase {
	
	@Autowired
	protected Scheduler scheduler;
	
	@Autowired
	protected ServerDbService serverDbService;
	
	/**
	 * 
	 * @param bm
	 * @param cronExpression
	 * @param jobClass
	 * @param jk
	 * @param tk
	 * @return true if created.
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public boolean createTrigger(BaseModel bm, String cronExpression, Class<? extends Job> jobClass, JobKey jk, TriggerKey tk) throws SchedulerException, ParseException {
		if (!StringUtil.hasAnyNonBlankWord(cronExpression)) return false;
		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(jobClass)
					.withIdentity(jk)
					.usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, bm.getId())
					.storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(cronExpression);
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
			return true;
		} else {
			if (scheduler.getTrigger(tk) == null) {
				CronExpression ce = new CronExpression(cronExpression);
				Trigger trigger = newTrigger().withIdentity(tk)
						.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
				scheduler.scheduleJob(trigger);
			}
		}
		return false;
	}
	
	protected void reschedule(BaseModel bm, String cronExpBefore, String cronExpAfter, Class<? extends Job> jobClass, JobKey jk, TriggerKey tk) throws SchedulerException, ParseException {
		if(cronExpBefore == null && cronExpAfter == null)return;
		if (cronExpBefore == null) { // cronExpAfter mustn't null.
			createTrigger(bm, cronExpAfter, jobClass, jk, tk);
		} else if (cronExpAfter == null) { // cronExpBefore mustn't null;
			scheduler.unscheduleJob(tk);
		} else if (!cronExpBefore.equals(cronExpAfter)) {
			CronExpression ce = new CronExpression(cronExpAfter);
			Trigger trigger = newTrigger().withIdentity(tk)
						.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.rescheduleJob(tk, trigger);
		} else if (cronExpBefore.equals(cronExpAfter)) {
			createTrigger(bm, cronExpAfter, jobClass, jk, tk);
		}
	}
	

}
