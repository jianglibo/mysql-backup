package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class MysqlFlushLogJob implements Job {
	
	@Autowired
	private ApplicationState applicationState;
	
	@Autowired
	private MysqlService mysqlTaskFacade;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private MysqlFlushService mysqlFlushService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		String host = data.getString("host");
		Box box = applicationState.getServerByHost(host);
		if (box == null) return;
		FacadeResult<String> fr = mysqlTaskFacade.mysqlFlushLogs(sshSessionFactory.getConnectedSession(box).getResult(), box);
		mysqlFlushService.processFlushResult(box, fr);
	}

}
