package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
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
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.PruneBackupedFiles;

@Component
public class BorgLocalRepoBackupJob implements Job {

	@Autowired
	private BorgService borgService;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private SettingsInDb settingsInDb;

	@Override
	@TrapException(BorgLocalRepoBackupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);

		Server sv = serverDbService.findById(sid);
		sv = serverDbService.loadFull(sv);

		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					doWrk(sv);
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

	private void doWrk(Server sv) throws IOException {
		borgService.backupLocalRepos(sv);
		
		BorgDescription bd = sv.getBorgDescription();
		String pruneStrategy = bd.getPruneStrategy();
		if (StringUtil.hasAnyNonBlankWord(pruneStrategy)) {
			new PruneBackupedFiles(settingsInDb.getRepoDir(sv)).prune(pruneStrategy);
		}
	}

}
