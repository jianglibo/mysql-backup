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
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class BorgBackupSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String BORG_ARCHIVE_GROUP = "BORG_ARCHIVE";

	public static final String BORG_PRUNE_GROUP = "BORG_PRUNE";
	
	public static final String BORG_BACKUP_LOCAL_REPO = "BORG_BACKUP_LOCAL_REPO";
	
	public static final String BORG_BACKUP_LOCAL_REPO_PRUNE = "BORG_BACKUP_LOCAL_REPO_PRUNE";

	//@formatter:off
	
	@EventListener
	public void whenBorgDescriptionCreated(ModelCreatedEvent<BorgDescription> borgDescriptionCreatedEvent) throws SchedulerException, ParseException {
		BorgDescription bd = borgDescriptionCreatedEvent.getModel();
		Server server = serverDbService.findById(bd.getServerId());
		createTrigger(server,
				bd.getArchiveCron(),
				BorgArchiveJob.class,
				jobKey(server.getHost(), BORG_ARCHIVE_GROUP),
				triggerKey(server.getHost(), BORG_ARCHIVE_GROUP));

		createTrigger(server,
				bd.getPruneCron(),
				BorgPruneJob.class,
				jobKey(server.getHost(), BORG_PRUNE_GROUP),
				triggerKey(server.getHost(), BORG_PRUNE_GROUP));
		
		createTrigger(server,
				bd.getLocalBackupCron(),
				BorgLocalRepoBackupJob.class,
				jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO),
				triggerKey(server.getHost(), BORG_BACKUP_LOCAL_REPO));
		
		createTrigger(server,
				bd.getLocalBackupPruneCron(),
				BorgLocalRepoBackupPruneJob.class,
				jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO_PRUNE),
				triggerKey(server.getHost(), BORG_BACKUP_LOCAL_REPO_PRUNE));

	}
	
	@EventListener
	public void whenBorgDescriptionChanged(ModelChangedEvent<BorgDescription> borgDescriptionChangedEvent) throws SchedulerException, ParseException {
		BorgDescription before = borgDescriptionChangedEvent.getBefore();
		BorgDescription after = borgDescriptionChangedEvent.getAfter();
		Server server = serverDbService.findById(after.getServerId());
		reschedule(server,
				before.getArchiveCron(),
				after.getArchiveCron(),
				BorgArchiveJob.class,
				jobKey(server.getHost(), BORG_ARCHIVE_GROUP),
				triggerKey(server.getHost(), BORG_ARCHIVE_GROUP));
		
		reschedule(server,
				before.getPruneCron(),
				after.getPruneCron(),
				BorgPruneJob.class,
				jobKey(server.getHost(), BORG_PRUNE_GROUP),
				triggerKey(server.getHost(), BORG_PRUNE_GROUP));
		
		reschedule(server,
				before.getLocalBackupCron(),
				after.getLocalBackupCron(),
				BorgLocalRepoBackupJob.class,
				jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO),
				triggerKey(server.getHost(), BORG_BACKUP_LOCAL_REPO));
		
		reschedule(server,
				before.getLocalBackupPruneCron(),
				after.getLocalBackupPruneCron(),
				BorgLocalRepoBackupPruneJob.class,
				jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO_PRUNE),
				triggerKey(server.getHost(), BORG_BACKUP_LOCAL_REPO_PRUNE));
	}
	
	@EventListener
	public void whenBorgDescriptionDeleted(ModelDeletedEvent<BorgDescription> borgDescriptionDeletedEvent) throws SchedulerException, ParseException {
		BorgDescription bd = borgDescriptionDeletedEvent.getModel();
		Server server = serverDbService.findById(bd.getServerId());
		scheduler.unscheduleJob(triggerKey(server.getHost(), BORG_ARCHIVE_GROUP));
		scheduler.unscheduleJob(triggerKey(server.getHost(), BORG_PRUNE_GROUP));
		
		scheduler.deleteJob(jobKey(server.getHost(), BORG_ARCHIVE_GROUP));
		scheduler.deleteJob(jobKey(server.getHost(), BORG_PRUNE_GROUP));
		
		scheduler.deleteJob(jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO));
		scheduler.deleteJob(jobKey(server.getHost(), BORG_BACKUP_LOCAL_REPO_PRUNE));
	}
}
