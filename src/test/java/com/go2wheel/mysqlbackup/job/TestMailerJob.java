package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.MessagingException;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.jcraft.jsch.Session;

public class TestMailerJob extends JobBaseFort {
	
	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgPruneJob borgPruneJob;
	
	@Autowired
	private DiskfreeJob diskfreeJob;
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Autowired
	private UpTimeJob upTimeJob;
	
	@Autowired
	private MysqlService mysqlService;
	
	
	private UserServerGrp simulation() throws SchedulerException, InterruptedException {
		UserAccount ua = createUser();
		createServer();
		deleteAllJobs();
		
		createMysqlIntance();
		deleteAllJobs();
		
		createBorgDescription();
		deleteAllJobs();
		
		ServerGrp sg = new ServerGrp("default");
		sg = serverGrpService.save(sg);
		
		serverGrpService.addServer(sg, server);
		
		UserServerGrp usg = new UserServerGrp.UserServerGrpBuilder(ua.getId(), sg.getId(), A_VALID_CRON_EXPRESSION, "aname").build();
		usg = userServerGrpService.save(usg);
		deleteAllJobs();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, server.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		Session session = sshSessionFactory.getConnectedSession(server).getResult();
		mysqlService.mysqlDump(session, server, true);
		
		for(int i = 0; i< 3;i ++) {
			mysqlFlushLogJob.execute(context);
			Thread.sleep(1000);
		}
		
		for(int i = 0; i< 3;i ++) {
			borgArchiveJob.execute(context);
			Thread.sleep(1000);
		}
		
		borgPruneJob.execute(context);
		
		for(int i = 0; i< 3;i ++) {
			upTimeJob.execute(context);
			diskfreeJob.execute(context);
			Thread.sleep(1000);
		}
		return usg;
	}
	
	@Test
	public void t() throws MessagingException, SchedulerException, InterruptedException {
		
		UserServerGrp usg = simulation();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, usg.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		mailerJob.setMailer(new Mailer() {
			@Override
			public void sendMailWithInline(ServerGroupContext rc) throws MessagingException {
				System.out.println(rc);
				try {
					Path pa = Paths.get("notingit", "tplcontext.json");
					objectMapper.writeValue(pa.toFile(), rc);
					ServerGroupContext sgc = objectMapper.readValue(pa.toFile(), ServerGroupContext.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		mailerJob.execute(context);
	}
	
	@Test
	public void tObjectMapper() {
		String[] names = applicationContext.getBeanNamesForType(ObjectMapper.class);
		assertThat(names.length, equalTo(1));
	}

}
