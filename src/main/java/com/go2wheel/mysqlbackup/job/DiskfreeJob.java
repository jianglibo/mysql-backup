package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.DiskfreeService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.Session;

@Component
public class DiskfreeJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DiskfreeService diskfreeService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server server = serverDbService.findById(sid);
			
			FacadeResult<Session> fr = sshSessionFactory.getConnectedSession(server); 
			session = fr.getResult();
			if (session == null) {
				logger.error("Connecting to server {} failed. message is: {}", server.getHost(), fr.getMessage());
				return;
			}
			diskfreeService.getLinuxDiskfree(server, session);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
