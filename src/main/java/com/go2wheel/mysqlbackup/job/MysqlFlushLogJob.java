package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlFlushLogJob implements Job {
	
	@Autowired
	private MysqlService mysqlTaskFacade;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	@TrapException(MysqlFlushLogJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			Server server = serverDbService.findById(sid);
			server = serverDbService.loadFull(server);
			session = sshSessionFactory.getConnectedSession(server).getResult();
			FacadeResult<Path> fr = mysqlTaskFacade.mysqlFlushLogsAndReturnIndexFile(session, server);
			mysqlFlushDbService.processFlushResult(server, fr);
		} catch (JSchException | IOException | NoSuchAlgorithmException | UnExpectedInputException | UnExpectedContentException | MysqlAccessDeniedException e) {
			throw new ExceptionWrapper(e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
