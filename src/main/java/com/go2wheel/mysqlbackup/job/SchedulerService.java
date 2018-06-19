package com.go2wheel.mysqlbackup.job;

import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Service
public class SchedulerService {
	
	@Autowired
	private Scheduler scheduler;
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	public void schedulerRescheduleJob(String triggerKey, String cronExpression) throws SchedulerException, ParseException {
		String[] ss = triggerKey.split("\\.", 2);
		TriggerKey tk = triggerKey(ss[1], ss[0]);
		Trigger tg = scheduler.getTrigger(tk);
		JobKey jk = tg.getJobKey();

		CronExpression ce = new CronExpression(cronExpression);
		Trigger trigger = newTrigger().withIdentity(ss[1], ss[0])
				.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(jk).build();
		scheduler.rescheduleJob(tk, trigger);
	}

	public List<Trigger> getServerTriggers(Server server) throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.filter(jk -> jk.getName().equals(server.getHost()))
				.flatMap(jk -> {
					try {
						return scheduler.getTriggersOfJob(jk).stream();
					} catch (SchedulerException e) {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}
	

	public FacadeResult<?> delteBoxTriggers(TriggerKey triggerKey) {
//		String[] ss = triggerKey.split("\\.", 2);
//		if (ss.length != 2) {
//			throw new ShowToUserException("scheduler.key.malformed", "");
//		}
//		TriggerKey tk = triggerKey(ss[1], ss[0]);
		try {
			scheduler.unscheduleJob(triggerKey);
		} catch (SchedulerException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
			
		}
		return FacadeResult.doneExpectedResult();
	}

	public List<JobKey> getJobkeysOfServer(Server box) throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.filter(jk -> jk.getName().equals(box.getHost()))
				.collect(Collectors.toList());
	}
	
	public List<JobKey> getAllJobKeys() throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.collect(Collectors.toList());
	}

	public void unscheduleJob(TriggerKey triggerKey) throws SchedulerException {
		this.scheduler.unscheduleJob(triggerKey);
	}

	public List<Trigger> getAllTriggers() throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.flatMap(jk -> {
					try {
						return scheduler.getTriggersOfJob(jk).stream();
					} catch (SchedulerException e) {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}

	public void getDeleteJob(JobKey jobKey) throws SchedulerException {
		this.scheduler.deleteJob(jobKey);
	}

}
