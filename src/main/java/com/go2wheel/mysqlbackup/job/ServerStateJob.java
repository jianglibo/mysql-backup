package com.go2wheel.mysqlbackup.job;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.Server;

@Component
public class ServerStateJob implements Job {

	@Autowired
	private ServerDbService serverDbService;

	
	@Autowired
	private JobLogDbService jobLogDbService;


	@Override
	@TrapException(ServerStateJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server server = serverDbService.findById(sid);
//		lockWrapped(server, context.toString());
	}

//	public void lockWrapped(Server server, String context) {
//		Session session = null;
//		try {
//			if (server.supportSSH()) {
//				session = sshSessionFactory.getConnectedSession(server).getResult();
//				if (server.getCoreNumber() == 0) {
//					server.setCoreNumber(serverStateService.getCoreNumber(server, session));
//					serverDbService.save(server);
//				}
//			}
//			serverStateService.createServerState(server, session);
//		} catch (JSchException | UnExpectedOutputException | IOException e) {
//			JobLog jl = new JobLog(BorgLocalRepoBackupJob.class, context , e.getMessage());
//			jobLogDbService.save(jl);
//			throw new ExceptionWrapper(e);
//		} finally {
//			if (session != null) {
//				session.disconnect();
//			}
//		}
//	}
}
