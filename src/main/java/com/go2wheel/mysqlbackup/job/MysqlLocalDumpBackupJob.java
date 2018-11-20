package com.go2wheel.mysqlbackup.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;

@Component
public class MysqlLocalDumpBackupJob implements Job {

	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private SettingsInDb settingsInDb;

	@Autowired
	private JobLogDbService jobLogDbService;

	@Override
	@TrapException(MysqlLocalDumpBackupJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
//		JobDataMap data = context.getMergedJobDataMap();
//		int mid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
//		MysqlInstance mi = mysqlInstanceDbService.findById(mid);
//		Server server = serverDbService.findById(mi.getServerId());
//		try {
//			lockerRounded(server, mi);
//		} catch (JSchException | CommandNotFoundException | NoSuchAlgorithmException | UnExpectedOutputException |  UnExpectedInputException | RunRemoteCommandException |  MysqlAccessDeniedException | ScpException | AppNotStartedException e) {
//			JobLog jl = new JobLog(MysqlLocalDumpBackupJob.class, context.toString(), e.getMessage());
//			jobLogDbService.save(jl);
//			throw new ExceptionWrapper(e);
//		}
	}

//	public void lockerRounded(Server server, MysqlInstance mi) throws JobExecutionException, NoSuchAlgorithmException, UnExpectedOutputException, UnExpectedInputException, JSchException, MysqlAccessDeniedException, CommandNotFoundException, RunRemoteCommandException, ScpException, AppNotStartedException {
//		Server sv = serverDbService.findById(mi.getServerId());
//		sv.setMysqlInstance(mi);
//
//		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_MYSQL);
//		try {
//			if (lock.tryLock(10, TimeUnit.SECONDS)) {
//				try {
//					doWrk(sv, mi);
//				} catch (IOException e) {
//					throw new ExceptionWrapper(e);
//				} finally {
//					lock.unlock();
//				}
//			} else {
//				throw new JobExecutionException(true);
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

//	private void doWrk(Server server, MysqlInstance mi) throws IOException, JSchException, NoSuchAlgorithmException, UnExpectedOutputException, UnExpectedInputException, MysqlAccessDeniedException, CommandNotFoundException, RunRemoteCommandException, ScpException, AppNotStartedException {
//		Session session = null;
//		try {
//			session = sshSessionFactory.getConnectedSession(server).getResult();
//			mysqlService.dump(session, server);
//			String pruneStrategy = mi.getPruneStrategy();
//			if (StringUtil.hasAnyNonBlankWord(pruneStrategy)) {
//				new PruneBackupedFiles(settingsInDb.getDumpDirBase(server)).prune(pruneStrategy);
//			}
//		} finally {
//			if (session != null) {
//				session.disconnect();
//			}
//		}
//	}
}
