package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelChangedEvent;
import com.go2wheel.mysqlbackup.event.ModelAfterCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.value.Subscribe;

@Component
public class MailerSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MAILER_SUBSCRIBE = "MAILER_UA_SVG_GROUP";

//	@EventListener
//	public void whenSubscribeChanged(ModelChangedEvent<Subscribe> subscribeChangedEvent) throws SchedulerException, ParseException {
//		Subscribe before = subscribeChangedEvent.getBefore();
//		Subscribe after = subscribeChangedEvent.getAfter();
//		
//		TriggerKey tk = triggerKey(before.getId() + "", MAILER_SUBSCRIBE);
//		scheduler.unscheduleJob(tk);
//		
//		createTrigger(after
//				,after.getCronExpression()
//				,MailerJob.class
//				,jobKey(after.getId() + "", MAILER_SUBSCRIBE)
//				,triggerKey(after.getId() + "", MAILER_SUBSCRIBE));
//	}
//
//	@EventListener
//	public void whenSubscribeCreated(ModelAfterCreatedEvent<Subscribe> subscribeCreatedEvent) throws SchedulerException, ParseException {
//		Subscribe subscribe = subscribeCreatedEvent.getModel(); 
//		createTrigger(subscribe
//				,subscribe.getCronExpression()
//				,MailerJob.class
//				,jobKey(subscribe.getId() + "", MAILER_SUBSCRIBE)
//				,triggerKey(subscribe.getId() + "", MAILER_SUBSCRIBE));
//	}
//	
//	@EventListener
//	public void whenSubscribeDeleted(ModelDeletedEvent<Subscribe> subscribeDeletedEvent) throws SchedulerException {
//		TriggerKey tk = triggerKey(subscribeDeletedEvent.getModel().getId() + "", MAILER_SUBSCRIBE);
//		scheduler.unscheduleJob(tk);
//		JobKey jk = jobKey(tk.getName(), tk.getGroup());
//		scheduler.deleteJob(jk);
//	}
}
