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

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.ConfigFile;

@Component
public class PowershellExecutionJob implements Job {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
	@Autowired
	private MyAppSettings myAppSettings;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap data = context.getMergedJobDataMap();
			String configFileName = data.getString(CommonJobDataKey.JOB_DATA_KEY_ID);
			String psCmdKey = data.getString(CommonJobDataKey.JOB_DATA_PS_COMMAND_KEY);
			ConfigFile configFile = configFileLoader.getOne(configFileName);
			lockRounded(configFile, psCmdKey, context.toString());
		} catch (ExecutionException e) {
			ExceptionUtil.logErrorException(logger, e);
			e.printStackTrace();
		}
	}
	
	public void lockRounded(ConfigFile configFile, String psCmdKey, String context) throws JobExecutionException {
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

	private void doWrk(ConfigFile sv, String psCmdKey, String context) {
		PSUtil.invokePowershell(sv.getProcessBuilderNeededList().get(psCmdKey), myAppSettings.getConsoleCharset());
	}

}
