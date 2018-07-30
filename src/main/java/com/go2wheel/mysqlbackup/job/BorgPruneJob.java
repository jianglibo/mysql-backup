package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class BorgPruneJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BorgService borgTaskFacade;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	@TrapException(BorgPruneJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server server = serverDbService.findById(sid);
			server = serverDbService.loadFull(server);
			if (borgTaskFacade.isBorgNotReady(server)) {
				logger.error("Box {} is not ready for Prune.", server.getHost());
				return;
			}
			session = sshSessionFactory.getConnectedSession(server).getResult();
			borgTaskFacade.pruneRepo(session, server);
		} catch (JSchException e) {
			throw new ExceptionWrapper(e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
