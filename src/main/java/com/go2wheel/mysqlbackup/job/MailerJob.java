package com.go2wheel.mysqlbackup.job;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.service.DiskfreeDbService;
import com.go2wheel.mysqlbackup.service.JobErrorDbService;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.UpTimeDbService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;
import com.go2wheel.mysqlbackup.service.UserServerGrpDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UpTimeDbService upTimeDbService;

	@Autowired
	private UserServerGrpDbService userServerGrpDbService;

	@Autowired
	private UserAccountDbService userAccountDbService;

	@Autowired
	private ServerGrpDbService serverGrpDbService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;

	@Autowired
	private DiskfreeDbService diskfreeDbService;

	@Autowired
	private JobErrorDbService jobErrorDbService;

	@Autowired
	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	private Mailer mailer;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int userServerGrpId = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		UserServerGrp usg = userServerGrpDbService.findById(userServerGrpId);

		if (usg == null) {
			logger.error("Cannot find UserServerGrp with ID: {}", userServerGrpId);
			return;
		}

		ServerGrp sg = serverGrpDbService.findById(usg.getServerGrpId());
		UserAccount ua = userAccountDbService.findById(usg.getUserAccountId());

		List<Server> servers = serverGrpDbService.getServers(sg).stream().map(sv -> serverDbService.loadFull(sv))
				.collect(Collectors.toList());

		List<ServerContext> oscs = new ArrayList<>();

		for (Server server : servers) {
			List<UpTime> upTimes = upTimeDbService.getRecentItems(10);
			List<MysqlFlush> mysqlFlushs = mysqlFlushDbService.getRecentItems(5);
			List<Diskfree> diskfrees = diskfreeDbService.getRecentItems(5);
			List<JobError> jobErrors = jobErrorDbService.getRecentItems(5);
			List<MysqlDump> mysqlDumps = mysqlDumpDbService.getRecentItems(1);
			List<BorgDownload> borgDownloads = borgDownloadDbService.getRecentItems(5);
			ServerContext osc = new ServerContext(upTimes, mysqlFlushs, diskfrees, jobErrors, mysqlDumps,
					borgDownloads);
			osc.setServer(server);
			oscs.add(osc);
		}
		ServerGroupContext rc = new ServerGroupContext(oscs, ua, sg);
		try {
			mailer.sendMailWithInline(usg.getTemplate(), rc);
		} catch (MessagingException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}

	@Autowired
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;

	}

}
