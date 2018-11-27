package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelAfterCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class RobocopySchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String ARCHIVE_GROUP = "ARCHIVE";

	public static final String PRUNE_GROUP = "PRUNE";
	
	public static final String BACKUP_LOCAL_REPO = "BACKUP_LOCAL_REPO";
	
	//@formatter:off
	
//	@EventListener
//	public void whenRobocopyDescriptionCreated(ModelAfterCreatedEvent<RobocopyDescription> RobocopyDescriptionCreatedEvent) throws SchedulerException, ParseException {
//		RobocopyDescription bd = RobocopyDescriptionCreatedEvent.getModel();
//		Server server = serverDbService.findById(bd.getServerId());
//		createTrigger(bd,
//				bd.getInvokeCron(),
//				RobocopyInvokeJob.class,
//				jobKey(server.getHost(), ARCHIVE_GROUP),
//				triggerKey(server.getHost(), ARCHIVE_GROUP));
//
//		createTrigger(bd,
//				bd.getLocalBackupCron(),
//				RobocopyLocalRepoBackupJob.class,
//				jobKey(server.getHost(), BACKUP_LOCAL_REPO),
//				triggerKey(server.getHost(), BACKUP_LOCAL_REPO));
//		
//	}
//	
//	@EventListener
//	public void whenRobocopyDescriptionChanged(ModelChangedEvent<RobocopyDescription> RobocopyDescriptionChangedEvent) throws SchedulerException, ParseException {
//		RobocopyDescription before = RobocopyDescriptionChangedEvent.getBefore();
//		RobocopyDescription after = RobocopyDescriptionChangedEvent.getAfter();
//		Server server = serverDbService.findById(after.getServerId());
//		reschedule(after,
//				before.getInvokeCron(),
//				after.getInvokeCron(),
//				RobocopyInvokeJob.class,
//				jobKey(server.getHost(), ARCHIVE_GROUP),
//				triggerKey(server.getHost(), ARCHIVE_GROUP));
//		
//		reschedule(after,
//				before.getLocalBackupCron(),
//				after.getLocalBackupCron(),
//				RobocopyLocalRepoBackupJob.class,
//				jobKey(server.getHost(), BACKUP_LOCAL_REPO),
//				triggerKey(server.getHost(), BACKUP_LOCAL_REPO));
//	}
//	
//	@EventListener
//	public void whenRobocopyDescriptionDeleted(ModelDeletedEvent<RobocopyDescription> RobocopyDescriptionDeletedEvent) throws SchedulerException, ParseException {
//		RobocopyDescription bd = RobocopyDescriptionDeletedEvent.getModel();
//		Server server = serverDbService.findById(bd.getServerId());
//		
//		scheduler.unscheduleJob(triggerKey(server.getHost(), ARCHIVE_GROUP));
//		
//		scheduler.deleteJob(jobKey(server.getHost(), ARCHIVE_GROUP));
//		
//		scheduler.deleteJob(jobKey(server.getHost(), BACKUP_LOCAL_REPO));
//	}
}
