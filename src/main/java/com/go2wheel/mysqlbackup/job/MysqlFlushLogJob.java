package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MysqlFlushLogJob implements Job {
	
	@Autowired
	private MysqlFlushLogService mysqlFlushLogService; 

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		mysqlFlushLogService.flushlogs();
	}

}
