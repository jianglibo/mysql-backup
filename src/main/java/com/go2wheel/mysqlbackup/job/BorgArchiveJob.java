package com.go2wheel.mysqlbackup.job;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.dbservice.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.TaskLocks;

@Component
public class BorgArchiveJob implements Job {

//	@Autowired
//	private BorgService borgService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private JobLogDbService jobLogDbService;

	@Override
	@TrapException(BorgArchiveJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Server sv = serverDbService.findById(sid);
		sv = serverDbService.loadFull(sv);
		lockRounded(sv, context.toString());
	}
	
	public void lockRounded(Server sv, String context) throws JobExecutionException {
		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
//					doWrk(sv, context);
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

//	private void doWrk(Server sv, String context) {
//		Session session = null;
//		try {
//			session = sshSessionFactory.getConnectedSession(sv).getResult();
//
//			FacadeResult<RemoteCommandResult> fr = borgService.archive(session, sv, false);
//			
//			if (!fr.isExpected()) {
//				throw new UnExpectedOutputException("10000", "borg.archived.failed", fr.getMessage());
//			}
//
//			long ts = fr.getEndTime() - fr.getStartTime();
//
//			BorgDownload bd = null;
//			FacadeResult<BorgDownload> frBorgDownload = borgService.downloadRepo(session, sv);
//			ts += frBorgDownload.getEndTime() - frBorgDownload.getStartTime();
//			bd = frBorgDownload.getResult();
//
//			bd.setServerId(sv.getId());
//			bd.setCreatedAt(new Date());
//			bd.setTimeCost(ts);
//			borgDownloadDbService.save(bd);
//
//		} catch (JSchException | CommandNotFoundException | NoSuchAlgorithmException | UnExpectedOutputException | IOException e) {
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
