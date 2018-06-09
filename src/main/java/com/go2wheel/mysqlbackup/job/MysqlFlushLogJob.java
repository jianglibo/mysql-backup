package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.Session;

@Component
public class MysqlFlushLogJob implements Job {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MysqlService mysqlTaskFacade;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server server = serverDbService.findById(sid);
			server = serverDbService.loadFull(server);
			
			if (mysqlTaskFacade.isMysqlNotReadyForBackup(server)) {
				logger.info("Box {} is not ready for Backup.", server.getHost());
				return;
			}
			session = sshSessionFactory.getConnectedSession(server).getResult();
			FacadeResult<String> fr = mysqlTaskFacade.mysqlFlushLogs(session, server);
			mysqlFlushDbService.processFlushResult(server, fr);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
