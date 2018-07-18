package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;

public class TestSubscribe extends JobBaseFort {
	
	private ServerGrp serverGrp;
	private UserAccount userAccount;
	
	@Before
	public void b() throws SchedulerException {
		clearDb();
		deleteAllJobs();
		assertThat(serverGrpDbService.count(), equalTo(0L));
		serverGrp = new ServerGrp("oneGroup");
		serverGrp = serverGrpDbService.save(serverGrp);
		assertThat(serverGrpDbService.count(), equalTo(1L));
		userAccount = createUser();
		assertThat(countJobs(), equalTo(0L));
	}

	@Test
	public void tShouldCreateJob() throws SchedulerException {
		Subscribe subscribe = new Subscribe.SubscribeBuilder(userAccount.getId(), serverGrp.getId(), A_VALID_CRON_EXPRESSION,
				"aname").build();
		subscribe = subscribeDbService.save(subscribe);
		assertThat(countJobs(), equalTo(1L));
		assertThat(countTriggers(), equalTo(1L));
		
		// remove subscribe.
		subscribeDbService.delete(subscribe);
		assertThat(countJobs(), equalTo(0L));
		assertThat(countTriggers(), equalTo(0L));
	}
	
	@Test
	public void tShouldUpdateJob() throws SchedulerException {
		Subscribe subscribe = new Subscribe.SubscribeBuilder(userAccount.getId(), serverGrp.getId(), A_VALID_CRON_EXPRESSION,
				"aname").build();
		subscribe = subscribeDbService.save(subscribe);
		assertThat(countJobs(), equalTo(1L));
		assertThat(countTriggers(), equalTo(1L));
		
		// update subscribe.
		subscribe.setCronExpression("0 0 3 1/1 * ?");
		subscribe = subscribeDbService.save(subscribe);
		assertThat(countJobs(), equalTo(1L));
		assertThat(countTriggers(), equalTo(1L));
		
		Trigger trigger = scheduler.getTrigger(triggerKey(subscribe.getId() + "", MailerSchedule.MAILER_SUBSCRIBE));
	}

}
