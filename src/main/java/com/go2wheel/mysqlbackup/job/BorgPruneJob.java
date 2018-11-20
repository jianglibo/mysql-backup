package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.service.ServerDbService;

@Component
public class BorgPruneJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerDbService serverDbService;

	@Override
	@TrapException(BorgPruneJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
//		Session session = null;
//		try {
//			JobDataMap data = context.getMergedJobDataMap();
//			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
//			Server server = serverDbService.findById(sid);
//			server = serverDbService.loadFull(server);
//			if (borgTaskFacade.isBorgNotReady(server)) {
//				logger.error("Box {} is not ready for Prune.", server.getHost());
//				return;
//			}
//			session = sshSessionFactory.getConnectedSession(server).getResult();
//			borgTaskFacade.pruneRepo(session, server);
//		} catch (JSchException | IOException e) {
//			throw new ExceptionWrapper(e);
//		} finally {
//			if (session != null) {
//				session.disconnect();
//			}
//		}
	}

}
