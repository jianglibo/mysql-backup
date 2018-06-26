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

import com.go2wheel.mysqlbackup.aop.TrapException;
import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerContext;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.service.JobErrorDbService;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.service.ServerStateDbService;
import com.go2wheel.mysqlbackup.service.StorageStateDbService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;
import com.go2wheel.mysqlbackup.service.UserServerGrpDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.DefaultValues;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServerStateDbService serverStateDbService;

	@Autowired
	private UserServerGrpDbService userServerGrpDbService;

	@Autowired
	private UserAccountDbService userAccountDbService;

	@Autowired
	private ServerGrpDbService serverGrpDbService;

	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private DefaultValues dvs;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;

	@Autowired
	private StorageStateDbService storageStateDbService;

	@Autowired
	private JobErrorDbService jobErrorDbService;

	@Autowired
	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	private Mailer mailer;

	@Override
	@TrapException(MailerJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int userServerGrpId = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		UserServerGrp userServerGrp = userServerGrpDbService.findById(userServerGrpId);
		ServerGroupContext sgctx = createMailerContext(userServerGrp);
		mail(sgctx.getUser().getEmail(), userServerGrp.getTemplate(), sgctx);
	}
	
	public ServerGroupContext createMailerContext(UserServerGrp userServerGrp) {
		ServerGrp sg = serverGrpDbService.findById(userServerGrp.getServerGrpId());
		UserAccount ua = userAccountDbService.findById(userServerGrp.getUserAccountId());

		List<Server> servers = serverGrpDbService.getServers(sg).stream().map(sv -> serverDbService.loadFull(sv))
				.collect(Collectors.toList());

		List<ServerContext> oscs = new ArrayList<>();

		for (Server server : servers) {
			ServerContext osc = makeServerContext(server);
			oscs.add(osc);
		}
		Server myself = serverDbService.findByHost("localhost");
		return new ServerGroupContext(oscs, ua, sg, makeServerContext(myself));

	}
	
	public ServerGroupContext createMailerContext(int userServerGrpId) {
		UserServerGrp usg = userServerGrpDbService.findById(userServerGrpId);
		return createMailerContext(usg);

	}

	private ServerContext makeServerContext(Server server) {
		List<ServerState> serverStates = serverStateDbService.getItemsInDays(server, dvs.getDefaultCount().getServerState());
		List<MysqlFlush> mysqlFlushs = mysqlFlushDbService.getRecentItems(server, dvs.getDefaultCount().getMysqlFlush());
		List<StorageState> storageStates = storageStateDbService.getItemsInDays(server, dvs.getDefaultCount().getStorageState());
		List<JobError> jobErrors = jobErrorDbService.getRecentItems(server, dvs.getDefaultCount().getJobError());
		List<MysqlDump> mysqlDumps = mysqlDumpDbService.getRecentItems(server, dvs.getDefaultCount().getMysqlDump());
		List<BorgDownload> borgDownloads = borgDownloadDbService.getRecentItems(server, dvs.getDefaultCount().getBorgDownload());
		ServerContext osc = new ServerContext(serverStates, mysqlFlushs, storageStates, jobErrors, mysqlDumps,
				borgDownloads);
		osc.setServer(server);
		return osc;
	}

	@Autowired
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}
	
	public void mail(String email, String template, ServerGroupContext sgctx) {
		try {
			this.mailer.sendMailWithInline(email, template, sgctx);
		} catch (MessagingException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}

	public String renderTemplate(String template, ServerGroupContext sgctx) {
		return mailer.renderTemplate(template, sgctx);
	}

}
