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
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class UpTimeSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String UPTIME_GROUP = "UPTIME_GROUP";

	//@formatter:off
	
	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server server = serverCreatedEvent.getModel();
		createTrigger(server,
				server.getUptimeCron(),
				UpTimeJob.class,
				jobKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP),
				triggerKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP));
	}
	
	@EventListener
	public void whenServerChanged(ModelChangedEvent<Server> modelChangedEvent) throws SchedulerException, ParseException {
		Server before = modelChangedEvent.getBefore();
		Server server = modelChangedEvent.getAfter();
		
		reschedule(server,
				before.getUptimeCron(),
				server.getUptimeCron(),
				UpTimeJob.class,
				jobKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP),
				triggerKey(server.getHost(), UpTimeSchedule.UPTIME_GROUP));
		
	}


}
