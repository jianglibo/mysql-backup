package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;

import javax.annotation.PostConstruct;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.model.KeyValue;

//@Component
public class CleanUpSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String CLEAN_UP = "CLEAN_UP_GROUP";
	
	public static final String SETTING_KEY = "app.cron.cleanup";
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@PostConstruct
	public void after() throws SchedulerException, ParseException {
		String expre = settingsInDb.getString(SETTING_KEY, "0 30 2 * * ? *");
		JobKey jk = jobKey(CLEAN_UP);
		TriggerKey tk = triggerKey(CLEAN_UP);
		JobDetail job = scheduler.getJobDetail(jk);
		
		if (job == null) {
			job = newJob(CleanupJob.class)
					.withIdentity(jk)
					.storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(expre);
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}
	
	
	@EventListener
	public void whenCleanCronChanged(ModelChangedEvent<KeyValue> kvChangedEvent) throws SchedulerException, ParseException {
		KeyValue before = kvChangedEvent.getBefore();
		KeyValue after = kvChangedEvent.getAfter();
		JobKey jk = jobKey(CLEAN_UP);
		TriggerKey tk = triggerKey(CLEAN_UP);
		
		if (SETTING_KEY.equals(after.getItemKey())) {
			if (!after.getItemValue().equals(before.getItemValue())) {
				CronExpression ce = new CronExpression(after.getItemValue());
				Trigger trigger = newTrigger().withIdentity(tk)
							.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
				scheduler.rescheduleJob(tk, trigger);
			}
		}
	}

}
