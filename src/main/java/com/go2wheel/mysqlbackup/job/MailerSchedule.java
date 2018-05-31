package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;

@Component
public class MailerSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MAILER_UA_SVG_GROUP = "MAILER_UA_SVG_GROUP";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private UserServerGrpService userServerGrpService;
	
	@PostConstruct
	public void post() throws SchedulerException, ParseException {
		List<UserServerGrp> usgs = userServerGrpService.findAll();
		for (UserServerGrp usg : usgs) {
			scheduleTrigger(usg);
		}
		;
	}

	private void scheduleTrigger(UserServerGrp usg) throws SchedulerException, ParseException {
		
		JobKey jk = jobKey(usg.getId() + "", MAILER_UA_SVG_GROUP);
		TriggerKey tk = triggerKey(usg.getId() + "", MAILER_UA_SVG_GROUP);

		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(MailerJob.class).withIdentity(jk).usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, usg.getId()).storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(usg.getCronExpression());
			
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}
	
	@EventListener
	public void whenUserServerGrpChanged(ModelChangedEvent<UserServerGrp> usgChangedEvent) {
		
	}

	@EventListener
	public void whenUserServerGrpCreated(ModelCreatedEvent<UserServerGrp> usgCreatedEvent) throws SchedulerException, ParseException {
		scheduleTrigger(usgCreatedEvent.getModel());
	}
	
	@EventListener
	public void whenUserServerGrpDeleted(ModelDeletedEvent<UserServerGrp> usgDeletedEvent) throws SchedulerException {
		TriggerKey tk = triggerKey(usgDeletedEvent.getModel().getId() + "", MAILER_UA_SVG_GROUP);
		scheduler.unscheduleJob(tk);
	}
	

}
