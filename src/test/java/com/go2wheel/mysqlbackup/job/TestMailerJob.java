package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.mail.MessagingException;

import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.installer.BorgInstaller;
import com.go2wheel.mysqlbackup.mail.Mailer;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
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
	private MysqlFlushLogJob mysqlFlushLogJob;
	
	@Autowired
	private ServerStateJob serverStateJob;
	
	@Autowired
	private MysqlService mysqlService;
	
	
	@Autowired
	private BorgInstaller borgInstaller;
	
	private Software software;
	
	
	private Subscribe simulation() throws SchedulerException, InterruptedException, JSchException, IOException, NoSuchAlgorithmException, UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException, CommandNotFoundException {
		clearDb();
		UserAccount ua = createUser();
		createServer();
		deleteAllJobs();
		
		createMysqlIntance();
		deleteAllJobs();
		
		createBorgDescription();
		deleteAllJobs();
		
		createSession();
		borgInstaller.syncToDb();
		software = softwareDbService.findByName("BORG").get(0);
		
		borgInstaller.install(session, server, software, null);
		
		
		ServerGrp sg = new ServerGrp("default");
		sg = serverGrpDbService.save(sg);
		
		serverGrpDbService.addServer(sg, server);
		
		Subscribe usg = new Subscribe.SubscribeBuilder(ua.getId(), sg.getId(), A_VALID_CRON_EXPRESSION, "aname").build();
		usg = subscribeDbService.save(usg);
		deleteAllJobs();
		createServerData();
		createMyselfData();
		return usg;
	}
	
	private void createServerData() throws JobExecutionException, InterruptedException, JSchException, IOException, NoSuchAlgorithmException, UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException, CommandNotFoundException {
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, server.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		Session session = sshSessionFactory.getConnectedSession(server).getResult();
		mysqlService.mysqlDump(session, server);
		
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
	public void tCreateMailerContext() throws MessagingException, SchedulerException, InterruptedException, JSchException, IOException, NoSuchAlgorithmException, UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException, CommandNotFoundException {
		
		Subscribe subscribe = simulation();
		
		JobDataMap jdm = new JobDataMap();
		jdm.put(CommonJobDataKey.JOB_DATA_KEY_ID, subscribe.getId());
		given(context.getMergedJobDataMap()).willReturn(jdm);
		
		mailerJob.setMailer(new Mailer() {
			@Override
			public void sendMail(Subscribe subscribe, String email, String template, ServerGroupContext sgctx) throws MessagingException {
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

			@Override
			public void sendMailPlainText(String subject, String content, String email)
					throws MessagingException, UnsupportedEncodingException {
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
