package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.MessagingException;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.Session;

public class TestMailerJob extends JobBaseFort {
	
	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private BorgArchiveJob borgArchiveJob;
	
	@Autowired
	private BorgPruneJob borgPruneJob;
	
	@Autowired
	private StorageStateJob storageStateJob;
	
	@Autowired
	private BorgService borgService;
	
	@Autowired
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Autowired
	private ServerStateJob serverStateJob;
	
	@Autowired
	private MysqlService mysqlService;
	
	
	private UserServerGrp simulation() throws SchedulerException, InterruptedException {
		clearDb();
		UserAccount ua = createUser();
		createServer();
		deleteAllJobs();
		
		createMysqlIntance();
		deleteAllJobs();
		
		createBorgDescription();
		deleteAllJobs();
		
		createSession();
		borgService.install(session);
		
		
		ServerGrp sg = new ServerGrp("default");
		sg = serverGrpDbService.save(sg);
		
		serverGrpDbService.addServer(sg, server);
		
		UserServerGrp usg = new UserServerGrp.UserServerGrpBuilder(ua.getId(), sg.getId(), A_VALID_CRON_EXPRESSION, "aname").build();
		usg = userServerGrpDbService.save(usg);
		deleteAllJobs();
		createServerData();
		createMyselfData();
		return usg;
	}
	
	private void createServerData() throws JobExecutionException, InterruptedException {
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
			serverStateJob.execute(context);
			storageStateJob.execute(context);
			Thread.sleep(1000);
		}
		
	}

	private void createMyselfData() throws SchedulerException, InterruptedException {
		createServer("localhost");
		deleteAllJobs();
		
		Server sv = serverDbService.findByHost("localhost");
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, sv.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);

		for(int i = 0; i< 3;i ++) {
			serverStateJob.execute(context);
			storageStateJob.execute(context);
			Thread.sleep(1000);
		}
	}

	@Test
	public void tCreateMailerContext() throws MessagingException, SchedulerException, InterruptedException {
		
		UserServerGrp usg = simulation();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, usg.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		mailerJob.setMailer(new Mailer() {
			@Override
			public void sendMailWithInline(String email, String template, ServerGroupContext sgctx) throws MessagingException {
				System.out.println(sgctx);
				try {
					Path pa = Paths.get("templates", "tplcontext.yml");
					String s = YamlInstance.INSTANCE.yaml.dumpAsMap(sgctx);
					Files.write(pa, s.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public String renderTemplate(String template, ServerGroupContext rc) {
				return null;
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
