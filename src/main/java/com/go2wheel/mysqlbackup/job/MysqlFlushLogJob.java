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
import com.jcraft.jsch.Session;

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
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			String host = data.getString("host");
			Box box = applicationState.getServerByHost(host);
			if (box == null)
				return;
			session = sshSessionFactory.getConnectedSession(box).getResult();
			FacadeResult<String> fr = mysqlTaskFacade.mysqlFlushLogs(session, box);
			mysqlFlushService.processFlushResult(box, fr);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
