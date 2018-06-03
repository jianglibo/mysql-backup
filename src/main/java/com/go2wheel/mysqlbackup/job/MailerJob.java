package com.go2wheel.mysqlbackup.job;

import java.util.ArrayList;
import java.util.List;

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
import com.go2wheel.mysqlbackup.service.BorgDownloadService;
import com.go2wheel.mysqlbackup.service.DiskfreeService;
import com.go2wheel.mysqlbackup.service.JobErrorService;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.service.UpTimeService;
import com.go2wheel.mysqlbackup.service.UserAccountService;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UpTimeService upTimeService;
	
	@Autowired
	private UserServerGrpService userServerGrpService;
	
	@Autowired
	private UserAccountService userAccountService;
	
	@Autowired
	private ServerGrpService serverGrpService;
	
	@Autowired
	private MysqlFlushService mysqlFlushService;
	
	@Autowired
	private DiskfreeService diskfreeService;
	
	@Autowired
	private JobErrorService jobErrorService;
	
	@Autowired
	private MysqlDumpService mysqlDumpService;
	
	@Autowired
	private BorgDownloadService borgDownloadService;
	
	private Mailer mailer;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int userServerGrpId = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		UserServerGrp usg = userServerGrpService.findById(userServerGrpId);
		
		if (usg == null) {
			logger.error("Cannot find UserServerGrp with ID: {}", userServerGrpId);
			return;
		}
		
		ServerGrp sg = serverGrpService.findById(usg.getServerGrpId());
		UserAccount ua = userAccountService.findById(usg.getUserAccountId());
		
		List<Server> servers = serverGrpService.getServers(sg);
		
		List<ServerContext> oscs = new ArrayList<>();
		
		for (Server server : servers) {
			List<UpTime> upTimes = upTimeService.getRecentItems(10);
			List<MysqlFlush> mysqlFlushs = mysqlFlushService.getRecentItems(5);
			List<Diskfree> diskfrees = diskfreeService.getRecentItems(5);
			List<JobError> jobErrors =  jobErrorService.getRecentItems(5);
			List<MysqlDump> mysqlDumps = mysqlDumpService.getRecentItems(1);
			List<BorgDownload> borgDownloads = borgDownloadService.getRecentItems(5);
			ServerContext osc = new ServerContext(upTimes, mysqlFlushs, diskfrees, jobErrors, mysqlDumps, borgDownloads);
			osc.setServer(server);
			oscs.add(osc);
		}
		ServerGroupContext rc = new ServerGroupContext(oscs, ua, sg);
		try {
			mailer.sendMailWithInline(rc);
		} catch (MessagingException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}
	
	@Autowired
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
		
	}

}
