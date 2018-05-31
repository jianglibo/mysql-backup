package com.go2wheel.mysqlbackup.job;

import static org.mockito.BDDMockito.given;

import javax.mail.MessagingException;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;

public class TestMailerJob extends SpringBaseFort {
	
	@Autowired
	private MailerJob mailerJob;
	
	@Test
	public void t() throws JobExecutionException, MessagingException {
		UserAccount ua = new UserAccount.UserAccountBuilder("jianglibo", "jianglibo@hotmail.com").build();
		ServerGrp sg = new ServerGrp("default");
		ua = userAccountService.save(ua);
		sg = serverGrpService.save(sg);
		UserServerGrp usg = new UserServerGrp.UserServerGrpBuilder(ua.getId(), sg.getId()).withCronExpression(A_VALID_CRON_EXPRESSION).build();
		usg = userServerGrpService.save(usg);
		
		
		Server server = serverService.findByHost(HOST_DEFAULT);
		
		if (server == null) {
			server = new Server(HOST_DEFAULT);
			server = serverService.save(server);
		}
		
		serverGrpService.addServer(sg, server);
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, usg.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		mailerJob.setMailer(new Mailer() {
			@Override
			public void sendMailWithInline(ServerGroupContext rc) throws MessagingException {
				System.out.println(rc);
				
			}
		});
		
		mailerJob.execute(context);
	}

}
