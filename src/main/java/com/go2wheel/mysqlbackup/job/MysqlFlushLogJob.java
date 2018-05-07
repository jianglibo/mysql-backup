package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.commands.MysqlTaskFacade;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;

@Component
public class MysqlFlushLogJob implements Job {
	
	@Autowired
	private ApplicationState applicationState;
	
	@Autowired
	private MysqlTaskFacade mysqlTaskFacade;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		String host = data.getString("host");
		Box box = applicationState.getServerByHost(host);
		if (box == null) return;
		mysqlTaskFacade.mysqlFlushLogs(sshSessionFactory.getConnectedSession(box).get(), box);
	}

}
