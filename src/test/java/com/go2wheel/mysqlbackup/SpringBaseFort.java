package com.go2wheel.mysqlbackup;

import static com.go2wheel.mysqlbackup.jooqschema.tables.ServergrpAndServer.SERVERGRP_AND_SERVER;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.BackupFolderService;
import com.go2wheel.mysqlbackup.service.BackupFolderStateService;
import com.go2wheel.mysqlbackup.service.BorgDescriptionService;
import com.go2wheel.mysqlbackup.service.BorgDownloadService;
import com.go2wheel.mysqlbackup.service.DiskfreeService;
import com.go2wheel.mysqlbackup.service.JobErrorService;
import com.go2wheel.mysqlbackup.service.KeyValueInDbService;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceService;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.service.UpTimeService;
import com.go2wheel.mysqlbackup.service.UserAccountService;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.DefaultValues;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@SpringBootTest(classes = StartPointer.class, value = { "spring.shell.interactive.enabled=false", "spring.shell.command.quit.enabled=false" , "spring.profiles.active=dev" })
@RunWith(SpringRunner.class)
public class SpringBaseFort {
	public static final String HOST_DEFAULT = "192.168.33.110";
	protected static final String A_VALID_CRON_EXPRESSION = "0 0 0 1/1 * ?";
	
	@Autowired
	protected MyAppSettings myAppSettings;
	
	@Autowired
	protected DSLContext jooq;

	@Autowired
	protected Environment env;

	@Autowired
	protected Scheduler scheduler;
	
	@Autowired
	protected ServerService serverService;
	
	@Autowired
	protected UserServerGrpService userServerGrpService;
	
	@Autowired
	protected KeyValueInDbService keyValueInDbService;
	
	@Autowired
	protected MysqlInstanceService mysqlInstanceService;
	
	@Autowired
	protected BorgDescriptionService borgDescriptionService;
	
	@Autowired
	protected UserAccountService userAccountService;
	
	@Autowired
	protected MysqlFlushService mysqlFlushService;
	
	@Autowired
	protected MysqlDumpService mysqlDumpService;
	
	@Autowired
	protected BackupFolderService backupFolderService;
	
	@Autowired
	protected BackupFolderStateService backupFolderStateService;
	
	@Autowired
	protected ReuseableCronService reuseableCronService;
	
	@Autowired
	protected DiskfreeService diskfreeService;
	
	@Autowired
	protected UpTimeService upTimeService;

	@Autowired
	protected BorgDownloadService borgDownloadService;
	
	@Autowired
	protected JobErrorService jobErrorService;
	
	@Autowired
	protected ServerGrpService serverGrpService;

	protected Server server;
	
	@Autowired
	private DefaultValues dvs;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	protected Session session;
	
	protected String TMP_SERVER_FILE_NAME = "/tmp/abc.txt";

	protected String TMP_SERVER_DIR_NAME = "/tmp/abc";

	protected String TMP_FILE_CONTENT = "abc";
	
	protected Path tmpDirectory;
	
	protected Path tmpFile;
	
	protected String remoteDemoFile;
	
	@Autowired
	protected FileDownloader fileDownloader;

	private long startTime;
	
	// this before run first.
	@Before
	public void beforeBase() throws SchedulerException {
//		UtilForTe.deleteAllJobs(scheduler);
		
		keyValueInDbService.deleteAll();
		mysqlInstanceService.deleteAll();
		borgDescriptionService.deleteAll();
		mysqlDumpService.deleteAll();
		mysqlFlushService.deleteAll();
		backupFolderStateService.deleteAll();
		backupFolderService.deleteAll();
		diskfreeService.deleteAll();
		upTimeService.deleteAll();
		borgDownloadService.deleteAll();
		jobErrorService.deleteAll();
		reuseableCronService.deleteAll();
		
		userServerGrpService.deleteAll();
		userAccountService.deleteAll();
		jooq.deleteFrom(SERVERGRP_AND_SERVER).execute();
		serverService.deleteAll();
		serverGrpService.deleteAll();
	}
	
	protected void createSession() {
		if (server == null) {
			createServer();
		}
		FacadeResult<Session> frs = sshSessionFactory.getConnectedSession(server);
		if (frs.isExpected()) {
			session = frs.getResult();
		}
	}
	
	
	@After
	public void afterBase() throws IOException, JSchException, RunRemoteCommandException {
		if (tmpDirectory != null) {
			try {
				FileUtil.deleteFolder(tmpDirectory);
			} catch (Exception e) {
			}
		}
		if (tmpFile != null) {
			Files.delete(tmpFile);
		}
		if (remoteDemoFile != null) {
			SSHcommonUtil.deleteRemoteFile(session, remoteDemoFile);
		}
		
		if (session != null) {
			session.disconnect();
		}
	}
	
	protected void createServer() {
		server = new Server(HOST_DEFAULT, "a server.");
		server = serverService.save(server);
	}
	
	protected UserAccount createUser() {
		UserAccount ua = new UserAccount.UserAccountBuilder("江立波", "jianglibo@gmail.com").build();
		return userAccountService.save(ua);
	}
	
	protected void createMysqlIntance() {
		MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), "123456")
				.addSetting("log_bin", "ON")
				.addSetting("log_bin_basename", "/var/lib/mysql/hm-log-bin")
				.addSetting("log_bin_index", "/var/lib/mysql/hm-log-bin.index")
				.withFlushLogCron(A_VALID_CRON_EXPRESSION)
				.build();
		server.setMysqlInstance(mysqlInstanceService.save(mi));
	}
	
	protected void createBorgDescription() {
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId())
				.addInclude("/etc").build();
		server.setBorgDescription(borgDescriptionService.save(bd));
	}
	
	protected void boxDeleteLogBinSetting() {
		server.getMysqlInstance().setLogBinSetting(null);
	}
	
	protected void time() {
		System.out.println(String.format("time elapsed: %s ms", System.currentTimeMillis() - startTime));
	}

	protected void createAfileOnServer(String rfile, String content) throws IOException, JSchException {
		remoteDemoFile = rfile;
		final Channel channel = session.openChannel("exec");
		try {
			((ChannelExec) channel).setCommand(String.format("echo %s > %s; cat %s", content,
					rfile, rfile));
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();

			RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getStdOut().trim(), equalTo(TMP_FILE_CONTENT));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
		} finally {
			channel.disconnect();
		}
	}



	protected void createADirOnServer(int number) throws IOException, JSchException {
		final Channel channel = session.openChannel("exec");
		StringBuilder sb = new StringBuilder(String.format("mkdir -p %s; rm -rf %s/*; mkdir %s/aabbcc",
				TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME, TMP_SERVER_DIR_NAME));
		for (int i = 0; i < number; i++) {
			sb.append(";");
			String s = String.format("echo %s > %s/%s", TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME,
					"sshbasefile_" + i + ".txt");
			sb.append(s);
		}
		sb.append(";");
		String s = String.format("echo %s > %s/aabbcc/%s", TMP_FILE_CONTENT, TMP_SERVER_DIR_NAME, "sshbasefile_x.txt");
		sb.append(s);
		try {
			
			((ChannelExec) channel).setCommand(sb.toString());
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();

			RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
			assertThat(cmdOut.getStdOut().trim(), equalTo(""));
			assertThat("exit code should be 0.", cmdOut.getExitValue(), equalTo(0));
		} finally {
			channel.disconnect();
		}
	}
	
	public void createALocalDir() throws IOException {
		tmpDirectory = Files.createTempDirectory("tmpfortest");
	}

	public void createALocalFile(String content) throws IOException {
		tmpFile = Files.createTempFile("sshbase", "txt");
		Files.write(tmpFile, content.getBytes());
	}

	public Path createALocalFileDirectory(int number) throws IOException {
		Path p = Files.createTempDirectory("sshbasedir");
		for (int i = 0; i < number; i++) {
			Path fp = p.resolve("sshbasefile_" + i + ".txt");
			Files.write(fp, TMP_FILE_CONTENT.getBytes());
		}
		Path nested = p.resolve("nested");
		Files.createDirectories(nested);
		Files.write(nested.resolve("a.txt"), TMP_FILE_CONTENT.getBytes());

		return p;
	}

	public void assertDirectory(Path topPath, long dirs, long files, long total) throws IOException {
		assertThat("directories should right.", Files.list(topPath).filter(Files::isDirectory).count(), equalTo(dirs));
		assertThat("files should right.", Files.list(topPath).filter(Files::isRegularFile).count(), equalTo(files));
		assertThat("total should right.", Files.walk(topPath).count(), equalTo(total));

	}
}


