package com.go2wheel.mysqlbackup.job;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.exception.IOExceptionWrapper;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Component
public class BorgArchiveJob implements Job {

	@Autowired
	private BorgService borgService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private MyAppSettings appSettings;
	
	@Autowired
	private SettingsInDb settingsInDb;

	@Override
	@TrapException(BorgArchiveJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {

		Session session = null;
		try {
			JobDataMap data = context.getMergedJobDataMap();
			int sid = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);

			Server sv = serverDbService.findById(sid);
			sv = serverDbService.loadFull(sv);

			session = sshSessionFactory.getConnectedSession(sv).getResult();

			FacadeResult<RemoteCommandResult> fr = borgService.archive(session, sv, false);

			long ts = fr.getEndTime() - fr.getStartTime();

			BorgDownload bd = null;
			FacadeResult<BorgDownload> frBorgDownload = borgService.downloadRepo(session, sv);
			ts += frBorgDownload.getEndTime() - frBorgDownload.getStartTime();
			bd = frBorgDownload.getResult();

			bd.setServerId(sv.getId());
			bd.setCreatedAt(new Date());
			bd.setTimeCost(ts);
			borgDownloadDbService.save(bd);
			
			final Path localRepo = appSettings.getBorgRepoDir(sv);
			FileUtil.backup(localRepo, 1, settingsInDb.getInteger("borg.repo.backups", 1), true);
		} catch (IOException e) {
			throw new IOExceptionWrapper(e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
