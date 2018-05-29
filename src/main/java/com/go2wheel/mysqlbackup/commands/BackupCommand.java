package com.go2wheel.mysqlbackup.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.ApplicationState.CommandStepState;
import com.go2wheel.mysqlbackup.DefaultValues;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.annotation.CronString;
import com.go2wheel.mysqlbackup.annotation.ShowDefaultValue;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.exception.NoServerSelectedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ShowToUserException;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserGrp;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.service.UserAccountService;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ShellCommonParameterValue;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.ToStringFormat;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.value.ResultEnum;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@ShellComponent()
public class BackupCommand {

	public static final String DESCRIPTION_FILENAME = "description.yml";
	
	public static final String DANGEROUS_ALERT = "I know what i am doing.";
	
	public static final int RESTART_CODE = 101;
	
	@Autowired
	private DefaultValues dvs;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MyAppSettings appSettings;

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationState appState;

	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private MysqlService mysqlService;

	private Session _session;

	@Autowired
	private BorgService borgService;

	@Autowired @Lazy
	private SchedulerService schedulerService;
	
	@Autowired
	private ServerGrpService serverGrpService;
	
	@Autowired
	private UserServerGrpService userServerGrpService;

	@Autowired
	private ReuseableCronService reusableCronService;

	@Autowired
	private MySqlInstaller mySqlInstaller;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private MysqlDumpService mysqlDumpService;
	
	@Autowired
	private MysqlFlushService mysqlFlushService;


	@Autowired
	private LocaledMessageService localedMessageService;

	@Autowired
	private BoxService boxService;

	@PostConstruct
	public void post() {

	}
	
	private Session getSession() {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		if (_session == null || !_session.isConnected()) {
			FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(box);
			if (frSession.isExpected()) {
				_session = frSession.getResult();
			} else {
				if (StringUtil.hasAnyNonBlankWord(frSession.getMessage())) {
					throw new ShowToUserException(frSession.getMessage(), frSession.getMessagePlaceHolders());
				} else if (frSession.getException() != null) {
					//will not get here.
				}
			}
		}
		return _session;
	}
	
	@ShellMethod(value = "Connect to target server.")
	private FacadeResult<?> ping(
			@ShellOption(help = "主机名") String host,
			@ShowDefaultValue()
			@ShellOption(help = "用户名", defaultValue="root") String username,
			@ShowDefaultValue()
			@ShellOption(help = "端口", defaultValue ="22") int port,
			@ShowDefaultValue()
			@ShellOption(help = "sshKey文件路径", defaultValue=ShellCommonParameterValue.NOT_EXIST_FILE) File sshKeyFile,
			@ShowDefaultValue()
			@ShellOption(help = "knowHosts文件路径", defaultValue=ShellCommonParameterValue.NOT_EXIST_FILE) File knownHostsFile,
			@ShowDefaultValue()
			@ShellOption(help = "密码", defaultValue = Box.NO_PASSWORD) String password) {
		File sshk = ShellCommonParameterValue.NOT_EXIST_FILE.equals(sshKeyFile.getName()) ? null : sshKeyFile;
		File knonwh = ShellCommonParameterValue.NOT_EXIST_FILE.equals(knownHostsFile.getName()) ? null : knownHostsFile;
		String ppaw = Box.NO_PASSWORD.equals(password) ? null : password;
		
		if (sshk == null && ppaw == null) {
			return FacadeResult.showMessageUnExpected("ssh.auth.noway");
		}
		
		if (sshk != null && knonwh == null) {
			return FacadeResult.showMessageUnExpected("ssh.auth.noknownhosts");
		}
		
		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(username, host, port, sshk, knonwh, ppaw);
		if (frSession.isExpected()) {
			Session session = frSession.getResult();
			try {
				SSHcommonUtil.runRemoteCommand(session, "echo hello");
				session.disconnect();
				return FacadeResult.doneExpectedResult("Success!", CommonActionResult.DONE);
			} catch (RunRemoteCommandException e) {
				return FacadeResult.unexpectedResult("Ping failed!");
			}
		} else {
			return frSession;
		}
	}


	@ShellMethod(value = "List all managed servers.")
	public ApplicationState serverList() throws IOException {
		return appState;
	}

	@ShellMethod(value = "Pickup a server to work on.")
	public ApplicationState serverSelect() throws IOException {
		this.appState.setStep(CommandStepState.WAITING_SELECT);
		return appState;
	}

	@ShellMethod(value = "新建一个服务器.")
	public FacadeResult<?> serverCreate(@ShellOption(help = "服务器主机名或者IP") String host) throws IOException {
		FacadeResult<Box> fr = boxService.serverCreate(host);
		if (fr.isExpected()) {
			appState.addServer(fr.getResult());
		}
		return fr;
	}

	@ShellMethod(value = "显示服务器描述")
	public FacadeResult<?> serverDetail() throws JSchException, IOException {
		sureBoxSelected();
		return FacadeResult.doneExpectedResult(appState.currentBoxOptional().get(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "和修改服务器描述")
	public FacadeResult<?> serverUpdate(@ShellOption(help = "用户名") String username,
			@ShellOption(help = "密码") String password, @ShellOption(help = "服务器角色") String boxRole,
			@ShellOption(help = "SSH端口") int port) throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		box.setUsername(username);
		box.setPassword(password);
		box.setRole(boxRole);
		box.setPort(port);
		boxService.writeDescription(box);
		return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
	}

	@ShellMethod(value = "显示配置相关信息。")
	public List<String> systemInfo(
			@ShellOption(help = "环境变量名", defaultValue="") String envname) throws IOException {
		if (StringUtil.hasAnyNonBlankWord(envname)) {
			return Arrays.asList(String.format("%s: %s", envname, environment.getProperty(envname)));
		}
		return Arrays.asList(formatKeyVal("server profile dirctory", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("database url", environment.getProperty("spring.datasource.url")),
				formatKeyVal("working directory", Paths.get("").toAbsolutePath().normalize().toString()),
				formatKeyVal("download directory", appSettings.getDownloadRoot().normalize().toAbsolutePath().toString()),
				formatKeyVal("log file", environment.getProperty("logging.file")),
				formatKeyVal("spring.config.name", environment.getProperty("spring.config.name")),
				formatKeyVal("spring.config.location", environment.getProperty("spring.config.location")),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
	}
	
	@ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
	public void quit(@ShellOption(help = "退出值", defaultValue="0") int exitValue,
			@ShellOption(help = "重启") boolean restart) {
		if (restart) {
			System.exit(101);
		}
		System.exit(exitValue);
	}


	@ShellMethod(value = "Upgrade system.")
	public FacadeResult<?> systemUpgrade(@ShellOption(help = "新版本的zip文件") File zipFile) {
		Path zp = zipFile.toPath();
		if (!Files.exists(zp)) {
			return FacadeResult.showMessageExpected(CommonMessageKeys.FILE_NOT_EXISTS, zipFile);
		}
		try {
			UpgradeUtil uu = new UpgradeUtil(zp);
			UpgradeFile uf = uu.writeUpgradeFile();
			if (!uf.isUpgradeable()) {
				return FacadeResult.showMessageExpected("command.upgrade.degrade", uf.getNewVersion(), uf.getCurrentVersion());
			}
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e, "command.upgrade.failed");
		}
		quit(0, true);
		return null;
	}

	@ShellMethod(value = "加载示例服务器。")
	public String loadDemoServer() throws IOException {
		try (InputStream is = ClassLoader.class.getResourceAsStream("/demobox.yml")) {
			Box box = YamlInstance.INSTANCE.yaml.loadAs(is, Box.class);
			if (appState.addServer(box)) {
			}
			return String.format("Demo server %s loaded.", box.getHost());
		}
	}

	private String formatKeyVal(String k, String v) {
		return String.format("%s: %s", k, v);
	}

	@EventListener
	public void whenServerChanged(ServerSwitchEvent sce) {
		if (_session != null) {
			try {
				_session.disconnect();
				_session = null;
			} catch (Exception e) {
				_session = null;
			}
		}
	}

	/**
	 * 1. check if already initialized. 2. get my.cnf content 3. check if
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSchException
	 */
	@ShellMethod(value = "为备份MYSQL作准备。")
	public FacadeResult<?> mysqlEnableLogbin(
			@ShowDefaultValue
			@ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。", defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME) String logBinValue)
			throws JSchException, IOException {
		sureMysqlConfigurated();
		return mysqlService.mysqlEnableLogbin(getSession(), appState.currentBoxOptional().get(), logBinValue);
	}

	@ShellMethod(value = "安装borg。")
	public FacadeResult<?> borgInstall() {
		sureBorgConfigurated();
		return borgService.install(getSession());
	}

	@ShellMethod(value = "管理Borg的描述")
	public FacadeResult<?> borgDescription(
			@ShowPossibleValue({"CREATE"})
			@ShellOption(help = "The action to take.", defaultValue="") String action) throws JSchException, IOException {
		
		switch (action) {
		case "CREATE":
			return borgDescriptionCreate();
		default:
			break;
		}
		sureBorgConfigurated();
		return FacadeResult.doneExpectedResult(appState.currentBoxOptional().get(), CommonActionResult.DONE);
	}

	public FacadeResult<?> borgDescriptionCreate() throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		BorgBackupDescription bbd = box.getBorgBackup();
		if (bbd != null) {
			FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
		}
		bbd = new BorgBackupDescription();
		bbd.setArchiveCron(dvs.getCron().getBorgArchive());
		bbd.setPruneCron(dvs.getCron().getBorgPrune());
		box.setBorgBackup(bbd);
		return borgService.saveBox(box);
	}

	@ShellMethod(value = "更新Borg的描述")
	public FacadeResult<?> borgDescriptionUpdate(@ShellOption(help = "borg repo.") String repo,
			@ShellOption(help = "borg archive format.") String archiveFormat,
			@ShellOption(help = "borg archive prefix.") String archiveNamePrefix,
			@ShellOption(help = "borg archive cron expression.") String archiveCron,
			@ShellOption(help = "borg prune cron expression.") String pruneCron) throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.updateBorgDescription(box, repo, archiveFormat, archiveNamePrefix, archiveCron, pruneCron);
	}
	
	
	@ShellMethod(value = "管理Borg的includes条目")
	public FacadeResult<?> borgDescriptionIncludes(
			@ShowPossibleValue({"ADD", "REMOVE"})
			@ShellOption(help = "The action to take.") @Pattern(regexp="ADD|REMOVE") String action,
			@ShellOption(help = "The directory to operate on.") String remoteDirectory
			) throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi.getIncludes() == null) bbdi.setIncludes(new ArrayList<>());
		FacadeResult<?> fr = baddremove(action, remoteDirectory, bbdi.getIncludes()); 
		if (fr.isExpected()) {
			return borgService.saveBox(box);
		}
		return fr;
	}
	
	private FacadeResult<?> baddremove(String action, String remoteDirectory, List<String> directories) {
		if ("ADD".equals(action)) {
			if (!directories.contains(remoteDirectory)) {
				try {
					boolean direxists = SSHcommonUtil.fileExists(getSession(), remoteDirectory);
					if (!direxists) {
						return FacadeResult.showMessageUnExpected("rfile.nonexists", remoteDirectory);
					}
					directories.add(remoteDirectory);
				} catch (RunRemoteCommandException e) {
					return FacadeResult.unexpectedResult(e);
				}
			}
		} else {
			if (directories.contains(remoteDirectory)) {
				directories.remove(remoteDirectory);
			}
		}
		return FacadeResult.doneExpectedResult();
		
	}
	
	@ShellMethod(value = "管理Borg的excludes条目")
	public FacadeResult<?> borgDescriptionExcludes(
			@ShowPossibleValue({"ADD", "REMOVE"})
			@ShellOption(help = "The action to take.") @Pattern(regexp="ADD|REMOVE") String action,
			@ShellOption(help = "The directory to operate on.") String remoteDirectory
			) throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi.getExcludes() == null) bbdi.setExcludes(new ArrayList<>());
		FacadeResult<?> fr = baddremove(action, remoteDirectory, bbdi.getExcludes()); 
		if (fr.isExpected()) {
			return borgService.saveBox(box);
		}
		return fr;
	}


	@ShellMethod(value = "安装MYSQL到目标机器")
	public String mysqlInstall(
			@ShowPossibleValue({"55", "56", "57", "80"})
			@ShellOption(help = "两位数的版本号比如，55,56,57,80。") @Pattern(regexp = "55|56|57|80") String twoDigitVersion,
			@ShellOption(help = "初始root的密码。") @Pattern(regexp = "[^\\s]{5,}") String initPassword) {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		FacadeResult<?> fr = mySqlInstaller.install(getSession(), box, twoDigitVersion, initPassword);
		if (!fr.isExpected()) {
			if (StringUtil.hasAnyNonBlankWord(fr.getMessage())) {
				return fr.getMessage();
			} else if (fr.getException() != null) {
				ExceptionUtil.logErrorException(logger, fr.getException());
				return fr.getException().getMessage();
			} else {
				return "安装失败";
			}
		}
		return "安装成功。";
	}

	@ShellMethod(value = "卸载目标机器的MYSQL")
	public FacadeResult<?> mysqlUninstall(@Pattern(regexp = "I know what i am doing\\.") String iknow) {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		return mySqlInstaller.unInstall(getSession(), box);
	}

	@ShellMethod(value = "初始化borg的repo。")
	public FacadeResult<?> borgRepoInit() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.initRepo(getSession(), box.getBorgBackup().getRepo());
	}
	
	@ShellMethod(value = "创建一次borg备份")
	public FacadeResult<?> borgArchiveCreate(
			@ShellOption(help = "try to solve comman problems.") boolean solveProblems
			) throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.archive(getSession(), box, solveProblems);
	}

	@ShellMethod(value = "下载borg的仓库。")
	public FacadeResult<?> borgRepoDownload() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.downloadRepo(getSession(), box);
	}

	@ShellMethod(value = "列出borg创建的卷")
	public List<String> borgArchiveList() {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.listArchives(getSession(), box).getResult().getArchives();
	}

	@ShellMethod(value = "修剪borg创建的卷")
	public String borgArchivePrune() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		BorgPruneResult bpr = borgService.pruneRepo(getSession(), box).getResult();
		return String.format("action: %s, pruned: %s, keeped: %s", bpr.isSuccess(), bpr.prunedArchiveNumbers(),
				bpr.keepedArchiveNumbers());
	}

	@ShellMethod(value = "列出borg仓库的文件，这些文件的意义由borg来解释。")
	public List<String> borgRepoListFiles() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBoxOptional().get();
		return borgService.listRepoFiles(getSession(), box).getResult().getAllTrimedNotEmptyLines();
	}

	private void sureBoxSelected() {
		if (!appState.currentBoxOptional().isPresent()) {
			throw new NoServerSelectedException(BackupCommandMsgKeys.SERVER_MISSING,
					"选择一个目标服务器先。 server-list, server-select.");
		}
	}

	private void sureBorgConfigurated() {
		sureBoxSelected();
		if (appState.currentBoxOptional().get().getBorgBackup() == null) {
			throw new ShowToUserException("borg.unconfigurated", "");
		}
	}

	private void sureMysqlConfigurated() {
		sureBoxSelected();
		if (appState.currentBoxOptional().get().getMysqlInstance() == null) {
			throw new ShowToUserException("mysql.unconfigurated", "");
		}
	}
	
	private void sureMysqlReadyForBackup() {
		sureMysqlConfigurated();
		Box box = appState.currentBoxOptional().get(); 
		if (box.getMysqlInstance() == null || box.getMysqlInstance().getLogBinSetting() == null) {
			throw new ShowToUserException("mysql.unreadyforbackup", box.getHost());
		}
	}

	@ShellMethod(value = "执行Mysqldump命令")
	public FacadeResult<?> mysqlDump() throws JSchException, IOException {
		sureMysqlReadyForBackup();
		Box box = appState.currentBoxOptional().get();
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(getSession(), box);
		MysqlDump md = new MysqlDump();
		md.setCreatedAt(new Date());
		md.setTimeCost(fr.getEndTime() - fr.getStartTime());
		if(fr.isExpected()) {
			if (fr.getResult() != null) {
				md.setFileSize(fr.getResult().getSize());
				md.setResult(ResultEnum.SUCCESS);
			} else if (MysqlService.ALREADY_DUMP.equals(fr.getMessage())) {
				md.setResult(ResultEnum.SKIP);
			} else {
				md.setResult(ResultEnum.UNKNOWN);
			}
		} else {
			md.setResult(ResultEnum.UNKNOWN);
		}
		Server sv = serverService.findByHost(box.getHost());
		md.setServerId(sv.getId());
		mysqlDumpService.save(md);
		return fr;
	}

	@ShellMethod(value = "添加或更改Mysql的描述")
	public FacadeResult<?> mysqlDescriptionUpdate(@ShellOption(help = "mysql username.") String username,
			@ShellOption(help = "mysql password.") String password, @ShellOption(help = "mysql port.") int port,
			@ShellOption(help = "mysql flush log cron expresion.") String flushLogCron)
			throws JSchException, IOException {
		sureMysqlConfigurated();
		Box box = appState.currentBoxOptional().get();
		return mysqlService.updateMysqlDescription(box, username, password, port, flushLogCron);
	}

	@ShellMethod(value = "创建Mysql的描述")
	public FacadeResult<?> mysqlDescriptionCreate(@ShowDefaultValue @ShellOption(help = "mysql username.", defaultValue="root") String username,
			@ShellOption(help = "mysql password.") String password) throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		MysqlInstance mi = box.getMysqlInstance();
		if (mi != null) {
			return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
		}
		mi = new MysqlInstance();
		mi.setUsername(username);
		mi.setPassword(password);
		mi.setFlushLogCron(dvs.getCron().getMysqlFlush());
		box.setMysqlInstance(mi);
		return mysqlService.updateMysqlDescription(box);
	}

	@ShellMethod(value = "手动flush Mysql的日志")
	public FacadeResult<?> MysqlFlushLog() {
		sureMysqlReadyForBackup();
		Box box = appState.currentBoxOptional().get();
		FacadeResult<String> fr = mysqlService.mysqlFlushLogs(getSession(), box); 
		mysqlFlushService.processFlushResult(box, fr);
		return fr;
	}

	@ShellMethod(value = "添加常用的CRON表达式")
	public String cronExpressionAdd(@ShellOption(help = "cron表达式") String expression,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		reusableCronService.save(new ReusableCron(expression, description));
		return "Added.";
	}

	@ShellMethod(value = "构建CRON表达式")
	public String cronExpressionBuild(
			@ShellOption(help = "支持的格式示例：1 或者 1,8,10 或者 5-25 或者 5/15(从5开始以15递增。)", defaultValue = "0") String second,
			@ShellOption(help = "格式同second参数，范围0-59", defaultValue = "0") String minute,
			@ShellOption(help = "格式同second参数，范围0-23", defaultValue = "0") String hour,
			@ShellOption(help = "格式同second参数，范围1-31", defaultValue = "?") String dayOfMonth,
			@ShellOption(help = "格式同second参数，范围1-12", defaultValue = "*") String month,
			@ShellOption(help = "格式同second参数，范围1-7，其中1是周六。", defaultValue = "*") String dayOfWeek,
			@ShellOption(help = "空白或者1970-2099", defaultValue = "") String year) {

		CronExpressionBuilder ceb = new CronExpressionBuilder();
		try {

			CronExpressionBuilder.validCronField(CronExpressionField.SECOND, second);
			ceb.second(second);

			CronExpressionBuilder.validCronField(CronExpressionField.MINUTE, minute);
			ceb.minute(minute);

			CronExpressionBuilder.validCronField(CronExpressionField.HOUR, hour);
			ceb.hour(hour);

			CronExpressionBuilder.validCronField(CronExpressionField.DAY_OF_MONTH, dayOfMonth);
			ceb.dayOfMonth(dayOfMonth);

			CronExpressionBuilder.validCronField(CronExpressionField.MONTH, month);
			ceb.month(month);

			CronExpressionBuilder.validCronField(CronExpressionField.DAY_OF_WEEK, dayOfWeek);
			ceb.dayOfWeek(dayOfWeek);
			CronExpressionBuilder.validCronField(CronExpressionField.YEAR, year);
			ceb.year(year);

			return ceb.build();
		} catch (InvalidCronExpressionFieldException e) {
			return e.getMessage();
		}
	}

	@ShellMethod(value = "列出常用的CRON表达式")
	public FacadeResult<?> cronExpressionList() {
		return FacadeResult.doneExpectedResult(reusableCronService.findAll(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "添加用户。")
	public FacadeResult<?> userAdd(
			@ShellOption(help = "用户名") String name,
			@ShellOption(help = "email地址") String email,
			@ShellOption(help = "手机号码", defaultValue=ShellOption.NULL) String mobile,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		UserAccount ua = new UserAccount.UserAccountBuilder(name, email).withMobile(mobile).withDescription(description).build();
		return FacadeResult.doneExpectedResult(userAccountService.save(ua), CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "用户列表。")
	public FacadeResult<?> userList() {
		return FacadeResult.doneExpectedResult(userAccountService.findAll(), CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "管理用户和服务器组的关系。")
	public FacadeResult<?> userAndServerGroup(
			@ShowPossibleValue({"LIST", "ADD", "REMOVE"})
			@ShellOption(help = "The action to take.") @Pattern(regexp="ADD|REMOVE|LIST") String action,
			@ShellOption(help = "用户名") @NotNull UserAccount user,
			@ShellOption(help = "服务器组") ServerGrp serverGroup,
			@CronString
			@ShellOption(help = "任务计划") String cron) {
		UserServerGrp usg;
		switch (action) {
		case "ADD":
			usg = new UserServerGrp.UserServerGrpBuilder(user.getId(), serverGroup.getId()).withCronExpression(cron).build();
			usg = userServerGrpService.save(usg);
			return FacadeResult.doneExpectedResult(usg, CommonActionResult.DONE);
		case "REMOVE":
			usg = userServerGrpService.findByUserAndServerGrp(user, serverGroup);
			userServerGrpService.delete(usg);
			return FacadeResult.doneExpectedResult(usg, CommonActionResult.DONE);
		default:
			break;
		}
		return FacadeResult.doneExpectedResult(userServerGrpService.findAll(), CommonActionResult.DONE);
	}

	
	
	@ShellMethod(value = "添加服务器组。")
	public FacadeResult<?> ServerGroupAdd(
			@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue=ShellOption.NULL) String msgkey) {
		ServerGrp sg = new ServerGrp(ename);
		sg.setMsgkey(msgkey);
		sg = serverGrpService.save(sg);
		return FacadeResult.doneExpectedResult(sg, CommonActionResult.DONE);
	}

	@ShellMethod(value = "列出服务器组。")
	public FacadeResult<?> ServerGroupList() {
		List<ServerGrp> sgs = serverGrpService.findAll();
		return FacadeResult.doneExpectedResult(sgs, CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "管理服务器组的主机")
	public FacadeResult<?> ServerGroupMembers(
			@ShowPossibleValue({"LIST", "ADD", "REMOVE"})
			@ShellOption(help = "The action to take.") @Pattern(regexp="ADD|REMOVE|LIST") String action,
			@ShellOption(help = "The server group to manage.") @NotNull ServerGrp serverGroup,
			@ShellOption(help = "The server to manage.", defaultValue=ShellOption.NULL) Server server
			) {
		switch (action) {
		case "ADD":
			if (server == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
			}
			serverGrpService.addServer(serverGroup, server);
			break;
		case "REMOVE":
			if (server == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
			}
			serverGrpService.removeServer(serverGroup, server);
			break;
		default:
			break;
		}
		return FacadeResult.doneExpectedResult(serverGrpService.getServers(serverGroup), CommonActionResult.DONE);
	}


	@ShellMethod(value = "添加用户组。")
	public FacadeResult<?> userGroupAdd(
			@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue=ShellOption.NULL) String msgkey) {
		UserGrp ug = new UserGrp(ename, msgkey);
		return null;
	}


	/**
	 * 再次执行Mysqldump命令之前必须确保mysql flushlogs任务已经结束。
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	@ShellMethod(value = "再次执行Mysqldump命令")
	public FacadeResult<?> mysqlDumpAgain(@ShellOption(defaultValue = "") String iknow)
			throws JSchException, IOException {
		sureMysqlReadyForBackup();
		Box box = appState.currentBoxOptional().get();
		if (!DANGEROUS_ALERT.equals(iknow)) {
			return FacadeResult.unexpectedResult("mysql.dump.again.wrongprompt");
		}
		return mysqlService.mysqlDump(getSession(), box, true);
	}

	private String getPromptString() {
		switch (appState.getStep()) {
		case WAITING_SELECT:
			return "Please choose an instance by preceding number: ";
		default:
			if (appState.currentBoxOptional().isPresent()) {
				return appState.currentBoxOptional().get().getHost() + "> ";
			} else {
				return "serverbackup> ";
			}
		}
	}

	@ShellMethod(value = "重新设置出发器")
	public void schedulerRescheduleJob(String triggerKey, String cronExpression)
			throws SchedulerException, ParseException {
		sureBoxSelected();
		schedulerService.schedulerRescheduleJob(triggerKey, cronExpression);
	}

	@ShellMethod(value = "支持的语言")
	public List<String> languageList() {
		List<String> las = new ArrayList<>();
		las.add(Locale.TRADITIONAL_CHINESE.getLanguage());
		las.add(Locale.ENGLISH.getLanguage());
		las.add(Locale.JAPANESE.getLanguage());
		return las;
	}

	@ShellMethod(value = "支持的语言")
	public String languageSet(String language) {
		if (!languageList().contains(language)) {
			return String.format("Supported languages are: %s", String.join(", ", languageList()));
		}
		Locale l = Locale.forLanguageTag(language);
		appState.setLocal(l);
		return "switch to language: " + language;
	}

	@ShellMethod(value = "列出当前主机的计划任务")
	public List<String> schedulerJobList() throws SchedulerException {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		return schedulerService.getBoxSchedulerJobList(box);

	}

	@ShellMethod(value = "列出当前主机的计划任务触发器")
	public List<String> schedulerTriggerList() throws SchedulerException {
		sureBoxSelected();
		return schedulerService.getBoxTriggers(appState.currentBoxOptional().get()).stream()
				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
	}

	@ShellMethod(value = "删除计划任务触发器")
	public FacadeResult<?> schedulerTriggerDelete(@ShellOption(help = "Trigger的名称。") String triggerKey) {
		sureBoxSelected();
		Box box = appState.currentBoxOptional().get();
		return schedulerService.delteBoxTriggers(box, triggerKey);
	}

	@ShellMethod(value = "查看最后一个命令的详细执行结果")
	public String facadeResultLast() {
		FacadeResult<?> fr = appState.getFacadeResult();
		if (fr == null) {
			return "";
		} else {
			if (fr.getException() != null) {
				return ExceptionUtil.stackTraceToString(fr.getException());
			} else if (fr.getResult() != null) {
				return fr.getResult().toString();
			} else if (fr.getMessage() != null && !fr.getMessage().isEmpty()) {
				return localedMessageService.getMessage(fr.getMessage());
			} else {
				return "";
			}
		}
	}

	// @ShellMethod(value = "Quartz 显示任务")
	// public List<String> quartzListJobs(@ShellOption(help = "任务组的名称", defaultValue
	// = "") String groupName)
	// throws SchedulerException {
	// if (groupName == null || groupName.trim().isEmpty()) {
	// return scheduler.getJobGroupNames().stream().flatMap(grn -> {
	// try {
	// return scheduler.getJobKeys(groupEquals(grn)).stream();
	// } catch (SchedulerException e) {
	// return Stream.empty();
	// }
	// }).map(jk -> jk.toString()).collect(Collectors.toList());
	// } else {
	// return scheduler.getJobKeys(groupEquals(groupName)).stream().map(jk ->
	// jk.toString())
	// .collect(Collectors.toList());
	// }

	// scheduler.standby();
	// shutdown() does not return until executing Jobs complete execution
	// scheduler.shutdown(true);
	// shutdown() returns immediately, but executing Jobs continue running to
	// completion
	// scheduler.shutdown();
	// or
	// scheduler.shutdown(false);

	// scheduler.unscheduleJob(triggerKey("trigger1", "group1"));

	// scheduler.deleteJob(jobKey("job1", "group1"));

	// // Define a durable job instance (durable jobs can exist without triggers)
	// JobDetail job1 = newJob(MyJobClass.class)
	// .withIdentity("job1", "group1")
	// .storeDurably()
	// .build();
	//
	// // Add the the job to the scheduler's store
	// sched.addJob(job, false);

	// // Define a Trigger that will fire "now" and associate it with the existing
	// job
	// Trigger trigger = newTrigger()
	// .withIdentity("trigger1", "group1")
	// .startNow()
	// .forJob(jobKey("job1", "group1"))
	// .build();
	//
	// // Schedule the trigger
	// sched.scheduleJob(trigger);

	// }
	// @ShellMethod(value = "Quartz 删除任务")
	// public void quartzDeleteJob(String triggerName, String triggerGroup) throws
	// SchedulerException {
	// scheduler.deleteJob(jobKey(triggerName, triggerGroup));
	// }

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
