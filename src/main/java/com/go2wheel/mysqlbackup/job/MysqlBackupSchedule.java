package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.event.ServerCreateEvent;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;

@Component
public class MysqlBackupSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MYSQL_GROUP = "MYSQL";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ApplicationState applicationState;

	@PostConstruct
	public void post() throws SchedulerException, ParseException {
		List<Box> mysqlBoxes = applicationState.getServers().stream()
				.filter(box -> box.getMysqlInstance() != null
						&& StringUtil.hasAnyNonBlankWord(box.getMysqlInstance().getFlushLogCron()))
				.collect(Collectors.toList());

		for (Box box : mysqlBoxes) {
			scheduleTrigger(box);
		}
		;
	}

	private void scheduleTrigger(Box box) throws SchedulerException, ParseException {
		JobKey jk = jobKey(box.getHost(), MYSQL_GROUP);

		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(MysqlFlushLogJob.class).withIdentity(jk).usingJobData("host", box.getHost()).storeDurably()
					.build();
			scheduler.addJob(job, false);

			CronExpression ce = new CronExpression(box.getMysqlInstance().getFlushLogCron());
			Trigger trigger = newTrigger().withIdentity(box.getHost(), MYSQL_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);

		}
	}

	@EventListener
	public void whenServerCreated(ServerCreateEvent sce) throws SchedulerException, ParseException {
//		scheduleTrigger(sce.getBox());
	}

}
