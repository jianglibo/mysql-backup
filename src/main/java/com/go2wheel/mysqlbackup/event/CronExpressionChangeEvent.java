package com.go2wheel.mysqlbackup.event;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationEvent;

public class CronExpressionChangeEvent extends ApplicationEvent {
	
	private final JobKey jobkey;
	
	private final TriggerKey triggerkey;
	
	private final String cron;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CronExpressionChangeEvent(Object source, JobKey jobkey, TriggerKey triggerkey, String cron) {
		super(source);
		this.jobkey = jobkey;
		this.triggerkey = triggerkey;
		this.cron = cron;
	}

	public JobKey getJobkey() {
		return jobkey;
	}

	public TriggerKey getTriggerkey() {
		return triggerkey;
	}

	public String getCron() {
		return cron;
	}
	
}
