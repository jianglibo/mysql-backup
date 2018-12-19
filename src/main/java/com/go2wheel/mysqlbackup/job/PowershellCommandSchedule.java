package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.ConfigFile;

@Component
public class PowershellCommandSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected Scheduler scheduler;

	public void schedule(ConfigFile configFile) throws SchedulerException, ParseException {
		configFile.getCrons().entrySet().stream().forEach(entry -> {
			try {
				createTrigger(configFile, entry.getKey(), entry.getValue(), PowershellExecutionJob.class,
						jobKey(configFile.getMypath(), entry.getKey()),
						triggerKey(configFile.getMypath(), entry.getKey()));
			} catch (SchedulerException | ParseException e) {
				String es = String.format("confile file: %s, cron: %s, value: %s had errors.", configFile.getMypath(),
						entry.getKey(), entry.getValue());
				logger.error(es);
				ExceptionUtil.logErrorException(logger, e);
				System.out.println(es);
			}
		});
	}

	public boolean createTrigger(ConfigFile configFile, String psCmdKey, String cronExpression,
			Class<? extends Job> jobClass, JobKey jk, TriggerKey tk) throws SchedulerException, ParseException {
		if (!StringUtil.hasAnyNonBlankWord(cronExpression))
			return false;
		JobDetail job = scheduler.getJobDetail(jk);
		if (job != null) {
			scheduler.deleteJob(jk);
		}
		// if (job == null) {
		job = newJob(jobClass).withIdentity(jk).usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, configFile.getMypath())
				.usingJobData(CommonJobDataKey.JOB_DATA_PS_COMMAND_KEY, psCmdKey).storeDurably().build();
		scheduler.addJob(job, false);

		CronExpression ce = new CronExpression(cronExpression);
		Trigger trigger = newTrigger().withIdentity(tk).withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk)
				.build();
		scheduler.scheduleJob(trigger);
		return true;
		// } else {
		// if (scheduler.getTrigger(tk) != null) {
		// scheduler.unscheduleJob(tk);
		// }
		// CronExpression ce = new CronExpression(cronExpression);
		// Trigger trigger =
		// newTrigger().withIdentity(tk).withSchedule(CronScheduleBuilder.cronSchedule(ce))
		// .forJob(jk).build();
		// scheduler.scheduleJob(trigger);
		// }
		// return false;
	}
}
