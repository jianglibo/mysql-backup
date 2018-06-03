package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;

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

import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.BoxUtil;

@Component
public class MysqlBackupSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MYSQL_FLUSH_LOG_GROUP = "MYSQL_FLUSH_LOG";

	@Autowired
	private Scheduler scheduler;

	@PostConstruct
	public void post() throws SchedulerException, ParseException {
		// List<Box> mysqlBoxes = applicationState.getBoxes().stream()
		// .filter(box -> box.getMysqlInstance() != null
		// && StringUtil.hasAnyNonBlankWord(box.getMysqlInstance().getFlushLogCron()))
		// .collect(Collectors.toList());
		//
		// for (Box box : mysqlBoxes) {
		// scheduleTrigger(box);
		// }
		// ;
	}

	// @formatter:off
	private void scheduleTrigger(Server box) throws SchedulerException, ParseException {
		JobKey jk = BoxUtil.getMysqlFlushLogJobKey(box);
		TriggerKey tk = BoxUtil.getMysqlFlushLogTriggerKey(box);

		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(MysqlFlushLogJob.class).withIdentity(jk)
					.usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, box.getId())
					.storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(box.getMysqlInstance().getFlushLogCron());
			Trigger trigger = newTrigger().withIdentity(tk).withSchedule(CronScheduleBuilder.cronSchedule(ce))
					.forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}

	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent)
			throws SchedulerException, ParseException {
		if (serverCreatedEvent.getModel().getMysqlInstance() != null) {
			scheduleTrigger(serverCreatedEvent.getModel());
		}
	}

}
