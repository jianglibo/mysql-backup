package com.go2wheel.mysqlbackup.job;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerStateService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.jcraft.jsch.Session;

@Component
public class ServerStateJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerStateService serverStateService;
	
	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server server = serverDbService.findById(sid);
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			if ("localhost".equals(server.getHost())) {
				serverStateService.createWinServerState(server, session);
			} else {
				serverStateService.createLinuxServerState(server, session);
			}
			
		} catch (RunRemoteCommandException | IOException e) {
			ExceptionUtil.logErrorException(logger, e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}
}
