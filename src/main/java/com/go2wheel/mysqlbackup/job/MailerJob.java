package com.go2wheel.mysqlbackup.job;

import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.Subscribe;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MailerJob implements Job {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private UserGroupLoader userGroupLoader;

  @Autowired
  private TemplateContextService templateContextService;

  private Mailer mailer;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap data = context.getMergedJobDataMap();
    String subscribeId = data.getString(CommonJobDataKey.JOB_DATA_KEY_ID);
    mail(subscribeId);
  }

  /**
   * send mail by subscribe object's id value.
   * @param subscribeId The Id value of subscribe.
   * @throws JobExecutionException exception.
   */

  public void mail(String subscribeId) throws JobExecutionException {
    Subscribe subscribe = userGroupLoader.getSubscribeById(subscribeId);
    ServerGroupContext sgctx;
    try {
      sgctx = templateContextService.createMailerContext(subscribe);
      mail(subscribe, sgctx.getUser().getEmail(), subscribe.getTemplate(), sgctx);
    } catch (UnsupportedEncodingException | MessagingException | ExecutionException e) {
      ExceptionUtil.logErrorException(logger, e);
      throw new JobExecutionException(e);
    }
  }
  
  public void mail(Subscribe subscribe, String email, String template, ServerGroupContext sgctx)
      throws UnsupportedEncodingException, MessagingException {
    this.mailer.sendMail(subscribe, email, template, sgctx);
  }

  @Autowired
  public void setMailer(Mailer mailer) {
    this.mailer = mailer;
  }


  public String renderTemplate(String template, ServerGroupContext sgctx) {
    return mailer.renderTemplate(template, sgctx);
  }

}
