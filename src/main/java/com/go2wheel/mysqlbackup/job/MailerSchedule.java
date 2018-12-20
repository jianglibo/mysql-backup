package com.go2wheel.mysqlbackup.job;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.go2wheel.mysqlbackup.value.Subscribe;
import java.text.ParseException;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailerSchedule {

  Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  protected Scheduler scheduler;

  //@formatter:off
  /**
   * use id as identity key.
   * @param subscribe subscribe object from {@link com.go2wheel.mysqlbackup.service.UserGroupLoader}
   * @throws SchedulerException schedulerexcption
   * @throws ParseException wrong cron expression.
   */
  public void schedule(Subscribe subscribe) throws SchedulerException, ParseException {
    JobKey jk = jobKey(subscribe.getId(), subscribe.getGroupname());
    final TriggerKey tk = triggerKey(subscribe.getId(), subscribe.getGroupname());
    JobDetail job = scheduler.getJobDetail(jk);
    if (job != null) {
      scheduler.deleteJob(jk);
    }
    // .usingJobData(CommonJobDataKey.JOB_DATA_KEY_GROUPNAME, subscribe.getGroupname())
    job = newJob(MailerJob.class).withIdentity(jk)
      .usingJobData(CommonJobDataKey.JOB_DATA_KEY_ID, subscribe.getId())
      .storeDurably()
      .build();
    scheduler.addJob(job, false);
    CronExpression ce = new CronExpression(subscribe.getCron());
    Trigger trigger = newTrigger()
        .withIdentity(tk)
        .withSchedule(CronScheduleBuilder.cronSchedule(ce))
        .forJob(jk)
        .build();
    scheduler.scheduleJob(trigger);
  }
}
