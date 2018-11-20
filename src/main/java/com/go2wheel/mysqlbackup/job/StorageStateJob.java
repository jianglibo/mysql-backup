package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.JobLogDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;

@Component
public class StorageStateJob implements Job {

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private JobLogDbService jobLogDbService;

	@Override
	@TrapException(StorageStateJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server server = serverDbService.findById(sid);
//		lockWrapped(server, context.toString());
	}

//	public List<StorageState> lockWrapped(Server server, String context) {
//		Session session = null;
//		try {
//			if (server.supportSSH()) {
//				session = sshSessionFactory.getConnectedSession(server).getResult();
//			}
//			return storageStateService.getStorageState(server, session);
//		} catch (JSchException | IOException e) {
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
