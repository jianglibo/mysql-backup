package com.go2wheel.mysqlbackup.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BorgDownloadService;
import com.go2wheel.mysqlbackup.service.JobErrorService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.ResultEnum;
import com.jcraft.jsch.Session;

@Component
public class BorgArchiveJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BorgService borgTaskFacade;

	@Autowired
	private BorgDownloadService borgDownloadService;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private JobErrorService jobErrorService;

	@Autowired
	private ServerService serverService;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
			
			Server sv = serverService.findById(sid);
			sv = serverService.loadFull(sv);
			
			JobError je = new JobError();
			je.setServerId(sv.getId());
			
			if (borgTaskFacade.isBorgNotReady(sv)) {
				logger.error("Box {} is not ready for Archive.", sv.getHost());
				je.setMessageDetail("borg not ready for backup.");
				jobErrorService.save(je);
				return;
			}
			
			session = sshSessionFactory.getConnectedSession(sv).getResult();

			FacadeResult<RemoteCommandResult> fr = borgTaskFacade.archive(session, sv, false);

			long ts = fr.getEndTime() - fr.getStartTime();
			
			
			
			BorgDownload bd = null;
			if (!fr.isExpected()) {
				if (fr.getResult() != null) {
					ExceptionUtil.logRemoteCommandResult(logger, fr.getResult());
				}
				je.setMessageKey(fr.getMessage());
				je.setMessageDetail(fr.resultToString());
				jobErrorService.save(je);
			} else {
				FacadeResult<BorgDownload> frBorgDownload = borgTaskFacade.downloadRepo(session, sv);
				ts += frBorgDownload.getEndTime() - frBorgDownload.getStartTime();
				if (frBorgDownload.isExpected()) {
					if (frBorgDownload.getResult() != null) {
						bd = frBorgDownload.getResult();
					}
				}
			}

			if (bd == null) {
				bd = new BorgDownload();
				bd.setResult(ResultEnum.FAIL);
			} else {
				bd.setResult(ResultEnum.SUCCESS);
			}
			
			bd.setServerId(sv.getId());
			bd.setCreatedAt(new Date());
			bd.setTimeCost(ts);
			borgDownloadService.save(bd);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
