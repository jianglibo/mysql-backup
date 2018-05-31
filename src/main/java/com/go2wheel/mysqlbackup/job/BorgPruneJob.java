package com.go2wheel.mysqlbackup.job;

import static org.quartz.TriggerKey.triggerKey;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

@Component
public class BorgPruneJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private BorgService borgTaskFacade;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private SchedulerService schedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			String host = data.getString("host");
			Box box = applicationState.getServerByHost(host);
			
			if (box == null) { //the box is somehow already removed.
				logger.error("The Box is somehow had removed. {}", host);
				schedulerService.unscheduleJob(triggerKey(host, BorgBackupSchedule.BORG_PRUNE_GROUP));
				return;
			}

			if (borgTaskFacade.isBorgNotReady(box)) {
				logger.error("Box {} is not ready for Prune.", host);
				return;
			}
			session = sshSessionFactory.getConnectedSession(box).getResult();
			borgTaskFacade.pruneRepo(session, box);
		} catch (SchedulerException e) {
			ExceptionUtil.logErrorException(logger, e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}

	}

}
