package com.go2wheel.mysqlbackup.job;

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
import com.go2wheel.mysqlbackup.dbservice.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.RobocopyService;
import com.go2wheel.mysqlbackup.util.TaskLocks;

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
	
	@Autowired
	private JobLogDbService jobLogDbService;
	
	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Override
	@TrapException(RobocopyLocalRepoBackupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int rid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);

		RobocopyDescription robocopyDescription = robocopyDescriptionDbService.findById(rid);
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(robocopyDescription.getId());
		robocopyDescription.setRobocopyItems(items);
		Server sv = serverDbService.findById(robocopyDescription.getServerId());
//
//		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
//		try {
//			if (lock.tryLock(10, TimeUnit.SECONDS)) {
//				try {
////					doWrk(context.toString(), sv, robocopyDescription);
//				} finally {
//					lock.unlock();
//				}
//			} else {
//				throw new JobExecutionException(true);
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

//	private void doWrk(String context, Server server, RobocopyDescription robocopyDescription) throws IOException {
//		Session session = null;
//		long start = System.currentTimeMillis();
//		try {
//			session = sshSessionFactory.getConnectedSession(server).getResult();
//			Path next = settingsInDb.getNextRepoDir(server);
//			Files.createDirectories(next);
//			Path backed = robocopyService.fullBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//			
//			if (backed != null) {
//				long ts = System.currentTimeMillis() - start;
//				BorgDownload bd = new BorgDownload();
//				bd.setServerId(server.getId());
//				bd.setDownloadBytes(Files.size(backed));
//				bd.setCreatedAt(new Date());
//				bd.setTimeCost(ts);
//				borgDownloadDbService.save(bd);
//			}
//			
//			String pruneStrategy = robocopyDescription.getPruneStrategy();
//			if (StringUtil.hasAnyNonBlankWord(pruneStrategy)) {
//				new PruneBackupedFiles(settingsInDb.getRepoDirBase(server)).prune(pruneStrategy);
//			}
//		} catch (JSchException | CommandNotFoundException | NoSuchAlgorithmException | UnExpectedOutputException | IOException | UnExpectedInputException | RunRemoteCommandException | ScpException e) {
//			JobLog jl = new JobLog(RobocopyInvokeJob.class, context, e.getMessage());
//			jobLogDbService.save(jl);
//			throw new ExceptionWrapper(e);
//		} finally {
//			if (session != null) {
//				session.disconnect();
//			}
//		}
//	}

}
