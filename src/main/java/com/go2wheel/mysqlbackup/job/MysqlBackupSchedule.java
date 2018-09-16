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
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class MysqlBackupSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MYSQL_FLUSH_LOG_GROUP = "MYSQL_FLUSH_LOG";

	// @formatter:off
	@EventListener
	public void whenMysqlInstanceCreated(ModelAfterCreatedEvent<MysqlInstance> mysqlInstanceCreatedEvent)
			throws SchedulerException, ParseException {
		MysqlInstance mi = mysqlInstanceCreatedEvent.getModel();
		Server server = serverDbService.findById(mi.getServerId());
		
		if (!Server.ROLE_GET.equals(server.getServerRole())) {
			return;
		}
		
		createTrigger(server,
				mi.getFlushLogCron(),
				MysqlFlushLogJob.class,
				jobKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP),
				triggerKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP));
	}

	@EventListener
	public void whenMysqlInstanceChanged(ModelChangedEvent<MysqlInstance> mysqlInstanceChangedEvent)
			throws SchedulerException, ParseException {
		MysqlInstance before = mysqlInstanceChangedEvent.getBefore();
		MysqlInstance after = mysqlInstanceChangedEvent.getAfter();
		Server server = serverDbService.findById(after.getServerId());

		if (!Server.ROLE_GET.equals(server.getServerRole())) {
			return;
		}
		
		reschedule(server,
				before.getFlushLogCron(),
				after.getFlushLogCron(),
				MysqlFlushLogJob.class,
				jobKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP),
				triggerKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP));
				
	}
	
	@EventListener
	public void whenMysqlInstanceDeleted(ModelDeletedEvent<MysqlInstance> mysqlInstanceDeletedEvent) throws SchedulerException, ParseException {
		MysqlInstance mi = mysqlInstanceDeletedEvent.getModel();
		Server server = serverDbService.findById(mi.getServerId());
		scheduler.unscheduleJob(triggerKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP));
		scheduler.deleteJob(jobKey(server.getHost(), MYSQL_FLUSH_LOG_GROUP));
	}
}
