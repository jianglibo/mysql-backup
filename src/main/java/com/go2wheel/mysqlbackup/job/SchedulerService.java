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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.event.CronExpressionChangeEvent;
import com.go2wheel.mysqlbackup.exception.ShowToUserException;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.Box;
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

	public List<Trigger> getBoxTriggers(Box box) throws SchedulerException {
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.filter(jk -> jk.getName().equals(box.getHost()))
				.flatMap(jk -> {
					try {
						return scheduler.getTriggersOfJob(jk).stream();
					} catch (SchedulerException e) {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}
	
	@EventListener
	public void onCronExpressionChange(CronExpressionChangeEvent cece) throws ParseException, SchedulerException {
		CronExpression ce = new CronExpression(cece.getCron());
		Trigger trigger = newTrigger().withIdentity(cece.getTriggerkey())
				.withSchedule(CronScheduleBuilder.cronSchedule(ce)).forJob(cece.getJobkey()).build();
		scheduler.rescheduleJob(cece.getTriggerkey(), trigger);
	}

	public FacadeResult<?> delteBoxTriggers(Box box, String triggerKey) {
		String[] ss = triggerKey.split("\\.", 2);
		if (ss.length != 2) {
			throw new ShowToUserException("scheduler.key.malformed", "");
		}
		TriggerKey tk = triggerKey(ss[1], ss[0]);
		try {
			scheduler.unscheduleJob(tk);
		} catch (SchedulerException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
			
		}
		return FacadeResult.doneExpectedResult();
	}

}
