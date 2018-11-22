package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
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
import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.RobocopyService;
import com.go2wheel.mysqlbackup.util.TaskLocks;

@Component
public class RobocopyInvokeJob implements Job {

	@Autowired
	private RobocopyService robocopyService;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	private JobLogDbService jobLogDbService;
	
	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Override
	@TrapException(RobocopyInvokeJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int rid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		RobocopyDescription rd = robocopyDescriptionDbService.findById(rid);
		
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(rd.getId());
		rd.setRobocopyItems(items);
		
		Server sv = serverDbService.findById(rd.getServerId());

//		Lock lock = TaskLocks.getBoxLock(sv.getHost(), TaskLocks.TASK_FILEBACKUP);
//		try {
//			if (lock.tryLock(10, TimeUnit.SECONDS)) {
//				try {
////					doWrk(context.toString(), sv, rd);
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

//	private void doWrk(String context, Server server, RobocopyDescription robocopyDescription) {
//		long start = System.currentTimeMillis();
//		Session session = null;
//		try {
//			session = sshSessionFactory.getConnectedSession(server).getResult();
//			Path filePath = robocopyService.incrementalBackupAndDownload(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//			if (filePath != null) {
//				long ts = System.currentTimeMillis() - start;
//				BorgDownload bd = new BorgDownload();
//				bd.setServerId(server.getId());
//				bd.setDownloadBytes(Files.size(filePath));
//				bd.setCreatedAt(new Date());
//				bd.setTimeCost(ts);
//				borgDownloadDbService.save(bd);
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
