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
public class borgBackupSchedule {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static final String BORG_GROUP = "BORG";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ApplicationState applicationState;

	@PostConstruct
	public void post() throws SchedulerException, ParseException {

		List<Box> borgBoxes = applicationState.getServers().stream()
				.filter(box -> box.getBorgBackup() != null
						&& StringUtil.hasAnyNonBlankWord(box.getBorgBackup().getCronExpression()))
				.collect(Collectors.toList());

		for (Box box : borgBoxes) {
			schudelerTrigger(box);
		}
	}

	private void schudelerTrigger(Box box) throws SchedulerException, ParseException {
		JobKey jk = jobKey(box.getHost(), BORG_GROUP);

		JobDetail job = scheduler.getJobDetail(jk);
		if (job == null) {
			job = newJob(MysqlFlushLogJob.class).withIdentity(jk).usingJobData("host", box.getHost()).storeDurably()
					.build();
			scheduler.addJob(job, false);
			CronExpression ce = new CronExpression(box.getMysqlInstance().getCronExpression());
			Trigger trigger = newTrigger().withIdentity(box.getHost(), BORG_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
			scheduler.scheduleJob(trigger);
		}
	}
	
	@EventListener
	public void whenServerCreated(ServerCreateEvent sce) throws SchedulerException, ParseException {
		schudelerTrigger(sce.getBox());
	}

}
