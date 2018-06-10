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
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class StorageStateSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String STORAGE_STATE_GROUP = "STORAGE_STATE";

	//@formatter:off
	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server server = serverCreatedEvent.getModel();
		createTrigger(server, server.getDiskfreeCron(), StorageStateJob.class, jobKey(server.getHost(), STORAGE_STATE_GROUP), triggerKey(server.getHost(), STORAGE_STATE_GROUP));
	}
	
	
	@EventListener
	public void whenServerChanged(ModelChangedEvent<Server> modelChangedEvent) throws SchedulerException, ParseException {
		Server before = modelChangedEvent.getBefore();
		Server server = modelChangedEvent.getAfter();
		
		reschedule(server,
				before.getDiskfreeCron(),
				server.getDiskfreeCron(),
				StorageStateJob.class,
				jobKey(server.getHost(), STORAGE_STATE_GROUP),
				triggerKey(server.getHost(), STORAGE_STATE_GROUP));
		
	}
	
	@EventListener
	public void whenServerDeleted(ModelDeletedEvent<Server> serverDeletedEvent) throws SchedulerException, ParseException {
		scheduler.unscheduleJob(triggerKey(serverDeletedEvent.getModel().getHost(), STORAGE_STATE_GROUP));
	}

}