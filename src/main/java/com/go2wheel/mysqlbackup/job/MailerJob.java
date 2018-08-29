package com.go2wheel.mysqlbackup.job;

import java.io.UnsupportedEncodingException;

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
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;

@Component
public class MailerJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SubscribeDbService userServerGrpDbService;


	@Autowired
	private TemplateContextService templateContextService;

	private Mailer mailer;

	@Override
	@TrapException(MailerJob.class)
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getMergedJobDataMap();
		int subscribeId = data.getInt(CommonJobDataKey.JOB_DATA_KEY_ID);
		Subscribe subscribe = userServerGrpDbService.findById(subscribeId);
		ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
		try {
			mail(subscribe, sgctx.getUser().getEmail(), subscribe.getTemplate(), sgctx);
		} catch (UnsupportedEncodingException | MessagingException e) {
			ExceptionUtil.logErrorException(logger, e);
			throw new JobExecutionException(e);
		}
	}
	
	@Autowired
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}
	
	public void mail(Subscribe subscribe, String email, String template, ServerGroupContext sgctx) throws UnsupportedEncodingException, MessagingException {
		this.mailer.sendMail(subscribe, email, template, sgctx);
	}

	public String renderTemplate(String template, ServerGroupContext sgctx) {
		return mailer.renderTemplate(template, sgctx);
	}

}
