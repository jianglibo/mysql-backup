package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlFlushLogJob implements Job {
	
	@Autowired
	private MysqlService mysqlService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	@TrapException(MysqlFlushLogJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server server = serverDbService.findById(sid);
			server = serverDbService.loadFull(server);
			
			lockerRounded(server, server.getMysqlInstance());
			
		} catch (JSchException | NoSuchAlgorithmException | UnExpectedInputException | UnExpectedOutputException | MysqlAccessDeniedException | CommandNotFoundException e) {
			throw new ExceptionWrapper(e);
		}
	}
	
	public void lockerRounded(Server server, MysqlInstance mi) throws JobExecutionException, NoSuchAlgorithmException, UnExpectedOutputException, UnExpectedInputException, JSchException, MysqlAccessDeniedException, CommandNotFoundException {
		server.setMysqlInstance(mi);
		Lock lock = TaskLocks.getBoxLock(server.getHost(), TaskLocks.TASK_MYSQL);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					doWrk(server, mi);
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
	

	private void doWrk(Server server, MysqlInstance mi) throws IOException, JSchException, NoSuchAlgorithmException, UnExpectedOutputException, UnExpectedInputException, MysqlAccessDeniedException, CommandNotFoundException {
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			FacadeResult<Path> fr = mysqlService.mysqlFlushLogsAndReturnIndexFile(session, server);
			mysqlFlushDbService.processFlushResult(server, fr);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
