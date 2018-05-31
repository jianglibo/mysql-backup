package com.go2wheel.mysqlbackup.job;

import static org.quartz.TriggerKey.triggerKey;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.DiskfreeService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.DiskFreeAllString;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.Session;

@Component
public class DiskfreeJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationState applicationState;

	@Autowired
	private DiskfreeService diskfreeService;

	@Autowired
	private ServerService serverService;

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
				schedulerService.unscheduleJob(triggerKey(host, DiskfreeSchedule.DISKFREE_GROUP));
				return;
			}
			FacadeResult<Session> fr = sshSessionFactory.getConnectedSession(box); 
			session = fr.getResult();
			if (session == null) {
				logger.error("Connecting to server {} failed. message is: {}", host, fr.getMessage());
				return;
			}
			List<DiskFreeAllString> dfss = SSHcommonUtil.getDiskUsage(session);
			List<Diskfree> dfs = dfss.stream().map(dd -> dd.toDiskfree()).collect(Collectors.toList());
			Server sv = serverService.findByHost(host);
			final Date d = new Date();
			if (sv != null) {
				dfs.stream().forEach(df -> {
					df.setCreatedAt(d);
					df.setServerId(sv.getId());
					diskfreeService.save(df);
				});
			}
		} catch (SchedulerException e) {
			ExceptionUtil.logErrorException(logger, e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
