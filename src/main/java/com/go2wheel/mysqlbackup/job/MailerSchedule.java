package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;

import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.model.UserServerGrp;

@Component
public class MailerSchedule extends SchedulerBase {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MAILER_UA_SVG_GROUP = "MAILER_UA_SVG_GROUP";

	@EventListener
	public void whenUserServerGrpChanged(ModelChangedEvent<UserServerGrp> usgChangedEvent) {
	}

	@EventListener
	public void whenUserServerGrpCreated(ModelCreatedEvent<UserServerGrp> usgCreatedEvent) throws SchedulerException, ParseException {
		UserServerGrp usg = usgCreatedEvent.getModel(); 
		createTrigger(usg
				,usg.getCronExpression()
				,MailerJob.class
				,jobKey(usg.getId() + "", MAILER_UA_SVG_GROUP)
				,triggerKey(usg.getId() + "", MAILER_UA_SVG_GROUP));
	}
	
	@EventListener
	public void whenUserServerGrpDeleted(ModelDeletedEvent<UserServerGrp> usgDeletedEvent) throws SchedulerException {
		TriggerKey tk = triggerKey(usgDeletedEvent.getModel().getId() + "", MAILER_UA_SVG_GROUP);
		scheduler.unscheduleJob(tk);
	}
}
