package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;

@Component
public class CleanupJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());


	@Override
	@TrapException(CleanupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("clean up run.");
	}

}
