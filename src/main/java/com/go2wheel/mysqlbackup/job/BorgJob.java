package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.borg.BorgTaskFacade;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;

@Component
public class BorgJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private BorgTaskFacade borgTaskFacade;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		String host = data.getString("host");
		Box box = applicationState.getServerByHost(host);
		try {
			borgTaskFacade.archive(sshSessionFactory.getConnectedSession(box).get(), box,
					StringUtil.notEmptyValue(box.getBorgBackup().getArchiveNamePrefix()).orElse("ARCHIVE-"));
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			throw new JobExecutionException(e);
		}
	}

}
