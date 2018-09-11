package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.JobLogDbService;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.PruneBackupedFiles;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class RobocopyLocalRepoBackupJob implements Job {

	@Autowired
	private RobocopyService robocopyService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Autowired
	private JobLogDbService jobLogDbService;

	@Override
	@TrapException(RobocopyLocalRepoBackupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int rid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);

		RobocopyDescription robocopyDescription = robocopyDescriptionDbService.findById(rid);
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(robocopyDescription.getId());
		robocopyDescription.setRobocopyItems(items);
		Server sv = serverDbService.findById(robocopyDescription.getServerId());

		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					doWrk(context, sv, robocopyDescription);
				} catch (IOException e) {
					throw new ExceptionWrapper(e);
				} finally {
					lock.unlock();
				}
			} else {
				throw new JobExecutionException(true);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void doWrk(JobExecutionContext context, Server server, RobocopyDescription robocopyDescription) throws IOException {
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			Path next = settingsInDb.getNextRepoDir(server);
			Files.createDirectories(next);
			robocopyService.fullBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
			String pruneStrategy = robocopyDescription.getPruneStrategy();
			if (StringUtil.hasAnyNonBlankWord(pruneStrategy)) {
				new PruneBackupedFiles(settingsInDb.getRepoDir(server)).prune(pruneStrategy);
			}
		} catch (JSchException | CommandNotFoundException | NoSuchAlgorithmException | UnExpectedContentException | IOException | UnExpectedInputException | RunRemoteCommandException | ScpException e) {
			JobLog jl = new JobLog(RobocopyInvokeJob.class, context, e.getMessage());
			jobLogDbService.save(jl);
			throw new ExceptionWrapper(e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
