package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.service.JobLogDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.StorageStateService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class StorageStateJob implements Job {

	@Autowired
	private StorageStateService storageStateService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private JobLogDbService jobLogDbService;

	@Override
	@TrapException(StorageStateJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server server = serverDbService.findById(sid);
		lockWrapped(server, context.toString());
	}

	public List<StorageState> lockWrapped(Server server, String context) {
		Session session = null;
		try {
			if (server.supportSSH()) {
				session = sshSessionFactory.getConnectedSession(server).getResult();
			}
			return storageStateService.getStorageState(server, session);
		} catch (JSchException | IOException e) {
			JobLog jl = new JobLog(BorgLocalRepoBackupJob.class, context , e.getMessage());
			jobLogDbService.save(jl);
			throw new ExceptionWrapper(e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
