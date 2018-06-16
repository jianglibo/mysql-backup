package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerStateService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.jcraft.jsch.Session;

@Component
public class ServerStateJob implements Job {

	@Autowired
	private ServerStateService serverStateService;
	
	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Override
	@TrapException(ServerStateJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server server = serverDbService.findById(sid);
		Session session = null;
		try {
			if (server.supportSSH()) {
				session = sshSessionFactory.getConnectedSession(server).getResult();
				if (server.getCoreNumber() == 0) {
					server.setCoreNumber(serverStateService.getCoreNumber(server, session));
					serverDbService.save(server);
				}
			}
			serverStateService.createServerState(server, session);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}
}
