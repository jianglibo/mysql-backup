package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.BoxUtil;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.DefaultValues;

@Component
public class DiskfreeSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String DISKFREE_GROUP = "DISKFREE_GROUP";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private DefaultValues dvs;

	@PostConstruct
	public void post() {
//		try {
//			List<Box> mysqlBoxes = applicationState.getBoxes();
//			for (Box box : mysqlBoxes) {
//				scheduleTrigger(box);
//			}
//			;
//		} catch (Exception e) {
//			ExceptionUtil.logErrorException(logger, e);
//		}
	}

	//@formatter:off
	private void scheduleTrigger(Server box) throws SchedulerException, ParseException {
		JobKey jk = BoxUtil.getDiskfreeJobKey(box);
		TriggerKey tk = BoxUtil.getDiskfreeTriggerKey(box);

		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(DiskfreeJob.class).withIdentity(jk)
					.usingJobData(CommonJobDataKey.JOB_DATA_KEY_HOST, box.getHost())
					.storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(dvs.getCron().getDiskfree());
			Trigger trigger = newTrigger().withIdentity(tk)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}

	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		scheduleTrigger(serverCreatedEvent.getModel());
	}
}
