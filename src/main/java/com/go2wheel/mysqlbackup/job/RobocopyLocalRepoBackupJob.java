package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
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
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.robocopy.RobocopyService;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.PruneBackupedFiles;

@Component
public class RobocopyLocalRepoBackupJob implements Job {

	@Autowired
	private RobocopyService robocopyService;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	private SettingsInDb settingsInDb;

	@Override
	@TrapException(RobocopyLocalRepoBackupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int rid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);

		RobocopyDescription rd = robocopyDescriptionDbService.findById(rid);
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(rd.getId());
		rd.setRobocopyItems(items);
		Server sv = serverDbService.findById(rd.getServerId());

		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					doWrk(sv, rd);
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

	private void doWrk(Server sv, RobocopyDescription rd) throws IOException {
		robocopyService.backupLocalRepos(sv);
		String pruneStrategy = rd.getPruneStrategy();
		if (StringUtil.hasAnyNonBlankWord(pruneStrategy)) {
			new PruneBackupedFiles(settingsInDb.getRepoDir(sv)).prune(pruneStrategy);
		}
	}

}
