package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.TrapException;

@Component
public class CleanupJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SettingsInDb settingsInDb;

	@Override
	@TrapException(CleanupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
//		int serverStateDeleted = serverStateDbService.deleteBefore(settingsInDb.getInteger("app.cleanup.max-keep-days", 90));
//		int storageStateDeleted = storageStateDbService.deleteBefore(settingsInDb.getInteger("app.cleanup.max-keep-days", 90));
//		logger.info("cleanup serverState: {}", serverStateDeleted);
//		logger.info("cleanup storageState: {}", storageStateDeleted);
	}

}
