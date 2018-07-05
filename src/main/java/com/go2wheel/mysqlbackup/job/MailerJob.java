package com.go2wheel.mysqlbackup.job;

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
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.UserServerGrpDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserServerGrpDbService userServerGrpDbService;


	@Autowired
	private TemplateContextService templateContextService;

	private Mailer mailer;

	@Override
	@TrapException(MailerJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int userServerGrpId = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		UserServerGrp userServerGrp = userServerGrpDbService.findById(userServerGrpId);
		ServerGroupContext sgctx = templateContextService.createMailerContext(userServerGrp);
		mail(sgctx.getUser().getEmail(), userServerGrp.getTemplate(), sgctx);
	}
	
//	public ServerGroupContext createMailerContext(UserServerGrp userServerGrp) {
//		ServerGrp sg = serverGrpDbService.findById(userServerGrp.getServerGrpId());
//		UserAccount ua = userAccountDbService.findById(userServerGrp.getUserAccountId());
//
//		List<Server> servers = serverGrpDbService.getServers(sg).stream().map(sv -> serverDbService.loadFull(sv))
//				.collect(Collectors.toList());
//
//		List<ServerContext> oscs = new ArrayList<>();
//
//		for (Server server : servers) {
//			ServerContext osc = makeServerContext(server);
//			oscs.add(osc);
//		}
//		Server myself = serverDbService.findByHost("localhost");
//		return new ServerGroupContext(oscs, ua, sg, makeServerContext(myself));
//
//	}
	
//	public ServerGroupContext createMailerContext(int userServerGrpId) {
//		UserServerGrp usg = userServerGrpDbService.findById(userServerGrpId);
//		return createMailerContext(usg);
//
//	}

//	private ServerContext makeServerContext(Server server) {
//		List<ServerState> serverStates = serverStateDbService.getItemsInDays(server, dvs.getDefaultCount().getServerState());
//		List<MysqlFlush> mysqlFlushs = mysqlFlushDbService.getRecentItems(server, dvs.getDefaultCount().getMysqlFlush());
//		List<StorageState> storageStates = storageStateDbService.getItemsInDays(server, dvs.getDefaultCount().getStorageState());
////		List<JobError> jobErrors = jobErrorDbService.getRecentItems(server, dvs.getDefaultCount().getJobError());
//		List<MysqlDump> mysqlDumps = mysqlDumpDbService.getRecentItems(server, dvs.getDefaultCount().getMysqlDump());
//		List<BorgDownload> borgDownloads = borgDownloadDbService.getRecentItems(server, dvs.getDefaultCount().getBorgDownload());
//		ServerContext osc = new ServerContext(serverStates, mysqlFlushs, storageStates, null, mysqlDumps,
//				borgDownloads);
//		osc.setServer(server);
//		return osc;
//	}

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
