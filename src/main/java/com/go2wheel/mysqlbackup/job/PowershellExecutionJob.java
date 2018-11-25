package com.go2wheel.mysqlbackup.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.NoActionException;
import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.ConfigFile;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

@Component
public class PowershellExecutionJob implements Job {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap data = context.getMergedJobDataMap();
			String configFileName = data.getString(CommonJobDataKey.JOB_DATA_KEY_ID);
			String psCmdKey = data.getString(CommonJobDataKey.JOB_DATA_PS_COMMAND_KEY);
			ConfigFile configFile = configFileLoader.getOne(configFileName);
			lockRounded(configFile, psCmdKey, context.toString());
		} catch (ExecutionException | NoActionException e) {
			ExceptionUtil.logErrorException(logger, e);
			e.printStackTrace();
		}
	}
	
	public void lockRounded(ConfigFile configFile, String psCmdKey, String context) throws JobExecutionException, ExecutionException, NoActionException {
		Lock lock = TaskLocks.getBoxLock(configFile.getMypath(), configFile.getAppName());
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					doWrk(configFile, psCmdKey, context);
				} finally {
					lock.unlock();
				}
			} else {
				throw new JobExecutionException(true);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void doWrk(ConfigFile configFile, String psCmdKey, String context) throws ExecutionException, NoActionException {
		ProcessExecResult per = configFileLoader.runCommand(configFile.getMypath(), psCmdKey);
		logger.debug("invoke configfile {}'s {} action. ", configFile.getMypath(), psCmdKey);
		logger.debug("get exit value: {}", per.getExitValue());
		logger.debug("*** stdout start ***");
		for(String line: per.getStdOut()) {
			logger.debug(line);
		}
		if (per.hasStdError()) {
			logger.error("invoke configfile {}'s {} action. ", configFile.getMypath(), psCmdKey);
			logger.error("*** stderr start ***");
			for(String line: per.getStdError()) {
				logger.error(line);
			}
		}
	}

}
