package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;

import javax.annotation.PostConstruct;

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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class BorgBackupSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String BORG_ARCHIVE_GROUP = "BORG_ARCHIVE";

	public static final String BORG_PRUNE_GROUP = "BORG_PRUNE";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ApplicationState applicationState;

	@PostConstruct
	public void post() throws SchedulerException, ParseException {
//		List<Box> borgBoxes = applicationState.getBoxes().stream()
//				.filter(box -> box.getBorgBackup() != null
//						&& StringUtil.hasAnyNonBlankWord(box.getBorgBackup().getArchiveCron()))
//				.collect(Collectors.toList());
//
//		for (Box box : borgBoxes) {
//			schudelerArchiveTrigger(box, BorgArchiveJob.class, box.getBorgBackup().getArchiveCron(),
//					BORG_ARCHIVE_GROUP);
//			schudelerArchiveTrigger(box, BorgPruneJob.class, box.getBorgBackup().getPruneCron(), BORG_PRUNE_GROUP);
//		}
	}

	//@formatter:off

	private void schudelerArchiveTrigger(Server server, Class<? extends Job> jobClass, String cronExp, String group) throws SchedulerException, ParseException {
		JobKey jk = jobKey(server.getHost(), group);
		TriggerKey tk = triggerKey(server.getHost(), group);
		
		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(jobClass)
					.withIdentity(jk)
					.usingJobData(CommonJobDataKey.JOB_DATA_KEY_HOST, server.getHost())
					.storeDurably()
					.build();
			scheduler.addJob(job, false);
			CronExpression ce = new CronExpression(cronExp);
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}
	

	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server server = serverCreatedEvent.getModel();
		BorgDescription bd = server.getBorgDescription();
		if (bd != null) {
			schudelerArchiveTrigger(server, BorgArchiveJob.class, bd.getArchiveCron(), BORG_ARCHIVE_GROUP);
			schudelerArchiveTrigger(server, BorgPruneJob.class, bd.getPruneCron(), BORG_PRUNE_GROUP);
		}
	}
	

}
