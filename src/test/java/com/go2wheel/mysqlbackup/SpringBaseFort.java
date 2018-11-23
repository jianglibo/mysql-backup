package com.go2wheel.mysqlbackup;

import static com.go2wheel.mysqlbackup.jooqschema.tables.ServergrpAndServer.SERVERGRP_AND_SERVER;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.dbservice.BigObDbService;
import com.go2wheel.mysqlbackup.dbservice.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.dbservice.JobLogDbService;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.dbservice.PlayBackDbService;
import com.go2wheel.mysqlbackup.dbservice.ReusableCronDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerGrpDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerStateDbService;
import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.dbservice.StorageStateDbService;
import com.go2wheel.mysqlbackup.dbservice.SubscribeDbService;
import com.go2wheel.mysqlbackup.dbservice.UserAccountDbService;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

//@formatter:off
@SpringBootTest(classes = StartPointer.class, 
value = { "spring.shell.interactive.enabled=false",
		"spring.shell.command.quit.enabled=false" ,
		"spring.profiles.active=dev" })
@RunWith(SpringRunner.class)
public class SpringBaseFort {
	public static final String HOST_DEFAULT_GET = "192.168.33.110";
	public static final String HOST_LOCAL_HOST = "localhost";
	public static final String HOST_DEFAULT_SET = "192.168.33.111";
	public static final String A_VALID_CRON_EXPRESSION = "0 0 0 1/1 * ?";
	
	@Autowired
	protected MyAppSettings myAppSettings;

	@Autowired
	protected SettingsInDb settingsIndb;
	
	@Autowired
	protected ObjectMapper objectMapper;
	
	@Autowired
	protected DSLContext jooq;
	
	
	@Autowired
	protected JobLogDbService jobLogDbService;
	
	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected Environment env;

	@Autowired
	protected Scheduler scheduler;
	
	@Autowired
	protected StorageStateDbService storageStateDbService;
	
	@Autowired
	protected ServerStateDbService serverStateDbService;
	
	@Autowired
	protected ServerDbService serverDbService;
	
	@Autowired
	protected PlayBackDbService playBackDbService;
	
	@Autowired
	protected SubscribeDbService subscribeDbService;
	
	@Autowired
	protected MysqlInstanceDbService mysqlInstanceDbService;
	
	@Autowired
	protected KeyValueDbService keyValueDbService;
	
	@Autowired
	protected RobocopyDescriptionDbService robocopyDescriptionDbService;
	
	@Autowired
	protected RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	protected BorgDescriptionDbService borgDescriptionDbService;
	
	@Autowired
	protected UserAccountDbService userAccountDbService;
	
	@Autowired
	protected BigObDbService bigObDbService;
	
	@Autowired
	protected ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	protected BorgDownloadDbService borgDownloadDbService;
	
	@Autowired
	protected ServerGrpDbService serverGrpDbService;
	
	@Autowired
	protected SoftwareDbService softwareDbService;

	protected Server server;
	
	protected String TMP_SERVER_FILE_NAME = "/tmp/abc.txt";

	
	@Autowired
	protected FileDownloader fileDownloader;

	private long startTime;
	
	// this before run first.
	@Before
	public void beforeBase() throws SchedulerException {
	}
	
	@After
	public void afterBase() throws IOException, RunRemoteCommandException {
		clearDb();
	}
	
	protected void clearDb() {
		softwareDbService.deleteAll();
		keyValueDbService.deleteAll();
		jobLogDbService.deleteAll();
		bigObDbService.deleteAll();
		playBackDbService.deleteAll();
		mysqlInstanceDbService.deleteAll();
		borgDescriptionDbService.deleteAll();
		serverStateDbService.deleteAll();
		storageStateDbService.deleteAll();
		borgDownloadDbService.deleteAll();
		reuseableCronDbService.deleteAll();
		subscribeDbService.deleteAll();
		userAccountDbService.deleteAll();
		jooq.deleteFrom(SERVERGRP_AND_SERVER).execute();
		robocopyItemDbService.deleteAll();
		robocopyDescriptionDbService.deleteAll();
		serverDbService.deleteAll();
		serverGrpDbService.deleteAll();
	}
	
	protected void deleteAllJobs() throws SchedulerException {
		scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream().forEach(jk -> {
			try {
				scheduler.deleteJob(jk);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		});
	}
	
	protected Server createServer() {
		server = createServer(HOST_DEFAULT_GET);
		return server;
	}
	
	protected Server createServerLocalhostWindows() {
		server = createServer("localhost");
		server.setOs("win");
		server.setUsername(System.getProperty("user.name"));
		return server;
	}
	
	protected Server createServer(String host) {
		return createServer(host, host, false);
	}
	
	protected Server createServer(String host, boolean set) {
		return createServer(host, host, set);
	}
	
	protected Server createServer(String host, String name, boolean set) {
		Server s = new Server(host, name);
		s.setServerRole(set ? "SET" : "GET");
		return serverDbService.save(s);
	}
	
	protected UserAccount createUser() {
		UserAccount ua = new UserAccount.UserAccountBuilder("江立波", "jianglibo@gmail.com").build();
		return userAccountDbService.save(ua);
	}
	
	protected void createMysqlIntance() {
		createMysqlIntance(server, "123456");
	}
	
	protected void createMysqlIntance(Server server, String initPassword) {
		MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), initPassword,
					"mysql",
					MysqlInstance.getDefaultDumpFileName(server.getOs()),
					MysqlInstance.getDefaultRestartCmd(server.getOs()))
				.addSetting("log_bin", "ON")
				.addSetting("log_bin_basename", "/var/lib/mysql/hm-log-bin")
				.addSetting("log_bin_index", "/var/lib/mysql/hm-log-bin.index")
				.withFlushLogCron(A_VALID_CRON_EXPRESSION)
				.build();
		server.setMysqlInstance(mysqlInstanceDbService.save(mi));
	}
	
	protected void createBorgDescription() {
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
				.addInclude("/etc").build();
		server.setBorgDescription(borgDescriptionDbService.save(bd));
	}
	
	protected void boxDeleteLogBinSetting() {
		server.getMysqlInstance().setLogBinSetting(null);
	}
	
	protected void time() {
		System.out.println(String.format("time elapsed: %s ms", System.currentTimeMillis() - startTime));
	}



	public Path createALocalFile(Path tmpFile, String content) throws IOException {
		Path parent = tmpFile.toAbsolutePath().getParent();
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}
		Files.write(tmpFile, content.getBytes());
		return tmpFile;
	}

	public Path createALocalFileDirectory(String content, int number) throws IOException {
		Path p = Files.createTempDirectory("sshbasedir");
		for (int i = 0; i < number; i++) {
			Path fp = p.resolve("sshbasefile_" + i + ".txt");
			Files.write(fp, content.getBytes());
		}
		Path nested = p.resolve("nested");
		Files.createDirectories(nested);
		Files.write(nested.resolve("a.txt"), content.getBytes());

		return p;
	}

	public void assertDirectory(Path topPath, long dirs, long files, long total) throws IOException {
		assertThat("directories should right.", Files.list(topPath).filter(Files::isDirectory).count(), equalTo(dirs));
		assertThat("files should right.", Files.list(topPath).filter(Files::isRegularFile).count(), equalTo(files));
		assertThat("total should right.", Files.walk(topPath).count(), equalTo(total));

	}
	
	public void printProcessExecutionResult(ProcessExecResult per) {
		System.out.println("standout:");
		for(String s : per.getStdOut()) {
			System.out.println(s);
		}
		System.out.println("standerr:");
		for(String s : per.getStdError()) {
			System.out.println(s);
		}
	}
	
	@Test
	public void tplaceholder() {
		assertTrue(true);
	}
	
	protected void createSessionLocalHostWindowsAfterClear() throws SchedulerException {
		clearDb();
		deleteAllJobs();
	}
}


