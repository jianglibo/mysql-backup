package com.go2wheel.mysqlbackup.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.annotation.CronStringIndicator;
import com.go2wheel.mysqlbackup.annotation.ObjectFieldIndicator;
import com.go2wheel.mysqlbackup.annotation.ServerHostPrompt;
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
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserGrp;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.service.BorgDescriptionService;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlFlushService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceService;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.service.UserAccountService;
import com.go2wheel.mysqlbackup.service.UserServerGrpService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ShellCommonParameterValue;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.ToStringFormat;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.DefaultValues;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.ResultEnum;
import com.go2wheel.mysqlbackup.vo.UserServerGrpVo;
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
	private MysqlInstanceService mysqlInstanceService;

	@Autowired
	private MysqlService mysqlService;

	private Session _session;

	@Autowired
	private BorgService borgService;

	@Autowired
	@Lazy
	private SchedulerService schedulerService;

	@Autowired
	private ServerGrpService serverGrpService;

	@Autowired
	private BorgDescriptionService borgDescriptionService;

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

	@PostConstruct
	public void post() {

	}

	// @formatter:off
	private Session getSession() {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		if (_session == null || !_session.isConnected()) {
			FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
			if (frSession.isExpected()) {
				_session = frSession.getResult();
			} else {
				if (StringUtil.hasAnyNonBlankWord(frSession.getMessage())) {
					throw new ShowToUserException(frSession.getMessage(), frSession.getMessagePlaceHolders());
				} else if (frSession.getException() != null) {
					// will not get here.
				}
			}
		}
		return _session;
	}

	@ShellMethod(value = "Connect to target server.")
	private FacadeResult<?> ping(@ShellOption(help = "主机名") String host,
			@ShowDefaultValue() @ShellOption(help = "用户名", defaultValue = "root") String username,
			@ShowDefaultValue() @ShellOption(help = "端口", defaultValue = "22") int port,
			@ShowDefaultValue() @ShellOption(help = "sshKey文件路径", defaultValue = ShellCommonParameterValue.NOT_EXIST_FILE) File sshKeyFile,
			@ShowDefaultValue() @ShellOption(help = "knowHosts文件路径", defaultValue = ShellCommonParameterValue.NOT_EXIST_FILE) File knownHostsFile,
			@ShowDefaultValue() @ShellOption(help = "密码", defaultValue = Box.NO_PASSWORD) String password) {
		File sshk = ShellCommonParameterValue.NOT_EXIST_FILE.equals(sshKeyFile.getName()) ? null : sshKeyFile;
		File knonwh = ShellCommonParameterValue.NOT_EXIST_FILE.equals(knownHostsFile.getName()) ? null : knownHostsFile;
		String ppaw = Box.NO_PASSWORD.equals(password) ? null : password;

		if (sshk == null && ppaw == null) {
			return FacadeResult.showMessageUnExpected("ssh.auth.noway");
		}

		if (sshk != null && knonwh == null) {
			return FacadeResult.showMessageUnExpected("ssh.auth.noknownhosts");
		}

		FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(username, host, port, sshk, knonwh,
				ppaw);
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
	public FacadeResult<?> serverList() throws IOException {
		return FacadeResult.doneExpectedResultDone(appState.getServers());
	}

	@ShellMethod(value = "Pickup a server to work on.")
	public FacadeResult<?> serverSelect(@ShellOption(help = "服务器主机名或者IP") Server server) throws IOException {
		appState.setCurrentServer(server);
		return null;
	}

	@ShellMethod(value = "新建一个服务器.")
	public FacadeResult<?> serverCreate(
			@ShellOption(help = "服务器主机名或者IP") String host,
			@ShellOption(help = "服务器的名称") String name) throws IOException {
		Server server = serverService.findByHost(host);
		if (server == null) {
			server = new Server(host, name);
			server = serverService.save(server);
		}
		return FacadeResult.doneExpectedResultDone(server);
	}
	
	@ShellMethod(value = "删除一个服务器.")
	public FacadeResult<?> serverDelete(
			@ServerHostPrompt @ShellOption(help = "服务器主机名或者IP") Server server,
			@ShellOption(defaultValue = "") String iknow) throws IOException {
		if (!DANGEROUS_ALERT.equals(iknow)) {
			return FacadeResult.unexpectedResult("mysql.dump.again.wrongprompt");
		}
		if (server == null) {
			return FacadeResult.showMessageExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, "");
		}
		serverService.delete(server);
		return FacadeResult.doneExpectedResult();
	}

	@ShellMethod(value = "显示服务器描述")
	public FacadeResult<?> serverDetail() throws JSchException, IOException {
		sureBoxSelected();
		return FacadeResult.doneExpectedResult(appState.currentServerOptional().get(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "和修改服务器描述")
	public FacadeResult<?> serverUpdate(
			@ShowPossibleValue({"host", "name" ,"port", "username", "password" ,"sshKeyFile", "serverRole", "uptimeCron", "diskfreeCron"})
			@ShellOption(help = "需要改变的属性") @Pattern(regexp = "host|name|port|username|password|sshKeyFile|serverRole|uptimeCron|diskfreeCron") String field,
			@ObjectFieldIndicator(objectClass=Server.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		Optional<Field> fo = ObjectUtil.getField(Server.class, field);
		Optional<Object> originOp = Optional.empty();
		
		value = ObjectUtil.getValueWetherIsToListRepresentationOrNot(value, field);
		try {
			if (fo.isPresent()) {
				originOp = Optional.ofNullable(fo.get().get(server));
				ObjectUtil.setValue(fo.get(), server, value);
				server = serverService.save(server);
			} else {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, field);
			}
		} catch (Exception e) {
			if (originOp.isPresent()) {
				try {
					fo.get().set(server, originOp.get());
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
			return FacadeResult.unexpectedResult(e);
		}
		return FacadeResult.doneExpectedResultDone(server);
	}

	@ShellMethod(value = "显示配置相关信息。")
	public List<String> systemInfo(@ShellOption(help = "环境变量名", defaultValue = "") String envname) throws IOException {
		if (StringUtil.hasAnyNonBlankWord(envname)) {
			return Arrays.asList(String.format("%s: %s", envname, environment.getProperty(envname)));
		}
		return Arrays.asList(
				formatKeyVal("server profile dirctory", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("database url", environment.getProperty("spring.datasource.url")),
				formatKeyVal("working directory", Paths.get("").toAbsolutePath().normalize().toString()),
				formatKeyVal("download directory",
						appSettings.getDownloadRoot().normalize().toAbsolutePath().toString()),
				formatKeyVal("log file", environment.getProperty("logging.file")),
				formatKeyVal("spring.config.name", environment.getProperty("spring.config.name")),
				formatKeyVal("spring.config.location", environment.getProperty("spring.config.location")),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
	}

	@ShellMethod(value = "Exit the shell.", key = { "quit", "exit" })
	public void quit(@ShellOption(help = "退出值", defaultValue = "0") int exitValue,
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
			return FacadeResult.showMessageExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, zipFile);
		}
		try {
			UpgradeUtil uu = new UpgradeUtil(zp);
			UpgradeFile uf = uu.writeUpgradeFile();
			if (!uf.isUpgradeable()) {
				return FacadeResult.showMessageExpected("command.upgrade.degrade", uf.getNewVersion(),
						uf.getCurrentVersion());
			}
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e, "command.upgrade.failed");
		}
		quit(0, true);
		return null;
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
			@ShowDefaultValue @ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。", defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME) String logBinValue)
			throws JSchException, IOException {
		sureMysqlConfigurated();
		return mysqlService.enableLogbin(getSession(), appState.currentServerOptional().get(), logBinValue);
	}

	@ShellMethod(value = "安装borg。")
	public FacadeResult<?> borgInstall() {
		sureBorgConfigurated();
		return borgService.install(getSession());
	}

	@ShellMethod(value = "创建Borg的描述")
	public FacadeResult<?> borgDescriptionCreate()
			throws JSchException, IOException {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		BorgDescription bbd = server.getBorgDescription();
		if (bbd != null) {
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		}
		bbd = new BorgDescription.BorgDescriptionBuilder(server.getId()).withArchiveCron(dvs.getCron().getBorgArchive())
				.withPruneCron(dvs.getCron().getBorgPrune()).build();
		bbd = borgDescriptionService.save(bbd);
		server.setBorgDescription(bbd);
		return FacadeResult.doneExpectedResultDone(bbd);
	}
	
	
	@ShellMethod(value = "更新Borg的描述")
	public FacadeResult<?> borgDescriptionUpdate(
			@ShowPossibleValue({"repo", "archiveCron", "pruneCron", "includes","excludes"})
			@ShellOption(help = "需要改变的属性, 其中includes和exludes使用:符号分割") @Pattern(regexp = "repo|archiveCron|pruneCron|includes|excludes") String field,
			@ObjectFieldIndicator(objectClass=BorgDescription.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		BorgDescription bd = server.getBorgDescription();
		
		Optional<Field> fo = ObjectUtil.getField(BorgDescription.class, field);
		Optional<Object> originOp = Optional.empty();
		try {
			if (fo.isPresent()) {
				originOp = Optional.ofNullable(fo.get().get(bd));
				switch (field) {
				case "includes":
				case "excludes":
					if (value == null) {
						fo.get().set(bd, new ArrayList<>());
					} else {
						fo.get().set(bd, Arrays.stream(value.split(":")).filter(s -> !s.trim().isEmpty()).collect(Collectors.toList()));
					}
					break;
				default:
					ObjectUtil.setValue(fo.get(), bd, value);
					break;
				}
				bd = borgDescriptionService.save(bd);
				server.setBorgDescription(bd);
			} else {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, field);
			}
		} catch (Exception e) {
			if (originOp.isPresent()) {
				try {
					fo.get().set(bd, originOp.get());
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
			return FacadeResult.unexpectedResult(e);
		}
		return FacadeResult.doneExpectedResultDone(bd);
	}

	@ShellMethod(value = "安装MYSQL到目标机器")
	public String mysqlInstall(@ShowPossibleValue({ "55", "56", "57",
			"80" }) @ShellOption(help = "两位数的版本号比如，55,56,57,80。") @Pattern(regexp = "55|56|57|80") String twoDigitVersion,
			@ShellOption(help = "初始root的密码。") @Pattern(regexp = "[^\\s]{5,}") String initPassword) {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		FacadeResult<?> fr = mySqlInstaller.install(getSession(), server, twoDigitVersion, initPassword);
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
		Server server = appState.currentServerOptional().get();
		return mySqlInstaller.unInstall(getSession(), server);
	}

	@ShellMethod(value = "初始化borg的repo。")
	public FacadeResult<?> borgRepoInit() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		return borgService.initRepo(getSession(), server.getBorgDescription().getRepo());
	}

	@ShellMethod(value = "创建一次borg备份")
	public FacadeResult<?> borgArchiveCreate(@ShellOption(help = "try to solve comman problems.") boolean solveProblems)
			throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		return borgService.archive(getSession(), server, solveProblems);
	}

	@ShellMethod(value = "下载borg的仓库。")
	public FacadeResult<?> borgRepoDownload() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		return borgService.downloadRepo(getSession(), server);
	}

	@ShellMethod(value = "列出borg创建的卷")
	public List<String> borgArchiveList() {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		return borgService.listArchives(getSession(), server).getResult().getArchives();
	}

	@ShellMethod(value = "修剪borg创建的卷")
	public String borgArchivePrune() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		BorgPruneResult bpr = borgService.pruneRepo(getSession(), server).getResult();
		return String.format("action: %s, pruned: %s, keeped: %s", bpr.isSuccess(), bpr.prunedArchiveNumbers(),
				bpr.keepedArchiveNumbers());
	}

	@ShellMethod(value = "列出borg仓库的文件，这些文件的意义由borg来解释。")
	public List<String> borgRepoListFiles() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.currentServerOptional().get();
		return borgService.listRepoFiles(getSession(), server).getResult().getAllTrimedNotEmptyLines();
	}

	private void sureBoxSelected() {
		if (!appState.currentServerOptional().isPresent()) {
			throw new NoServerSelectedException(BackupCommandMsgKeys.SERVER_MISSING,
					"选择一个目标服务器先。 server-list, server-select.");
		}
	}

	private void sureBorgConfigurated() {
		sureBoxSelected();
		if (appState.currentServerOptional().get().getBorgDescription() == null) {
			throw new ShowToUserException("borg.unconfigurated", "");
		}
	}

	private void sureMysqlConfigurated() {
		sureBoxSelected();
		if (appState.currentServerOptional().get().getMysqlInstance() == null) {
			throw new ShowToUserException("mysql.unconfigurated", "");
		}
	}

	private void sureMysqlReadyForBackup() {
		sureMysqlConfigurated();
		Server server = appState.currentServerOptional().get();
		if (server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null) {
			throw new ShowToUserException("mysql.unreadyforbackup", server.getHost());
		}
	}

	@ShellMethod(value = "执行Mysqldump命令")
	public FacadeResult<?> mysqlDump() throws JSchException, IOException {
		sureMysqlReadyForBackup();
		Server server = appState.currentServerOptional().get();
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(getSession(), server);
		saveDumpResult(server, fr);
		return fr;
	}
	
	/**
	 * 再次执行Mysqldump命令之前必须确保mysql flushlogs任务已经结束。
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	@ShellMethod(value = "再次执行Mysqldump命令")
	public FacadeResult<?> mysqlDumpAgain(@ShellOption(defaultValue = ShellOption.NULL) String iknow)
			throws JSchException, IOException {
		sureMysqlReadyForBackup();
		Server server = appState.currentServerOptional().get();
		if (!DANGEROUS_ALERT.equals(iknow)) {
			return FacadeResult.unexpectedResult("mysql.dump.again.wrongprompt");
		}
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(getSession(), server, true);
		saveDumpResult(server, fr);
		return fr;
	}


	private void saveDumpResult(Server server, FacadeResult<LinuxLsl> fr) {
		MysqlDump md = new MysqlDump();
		md.setCreatedAt(new Date());
		md.setTimeCost(fr.getEndTime() - fr.getStartTime());
		if (fr.isExpected()) {
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
		Server sv = serverService.findByHost(server.getHost());
		md.setServerId(sv.getId());
		mysqlDumpService.save(md);
	}
	
	@ShellMethod(value = "列出Mysqldump历史纪录")
	public FacadeResult<?> mysqlDumpList() throws JSchException, IOException {
		Server server = appState.currentServerOptional().get();
		List<MysqlDump> dumps = mysqlDumpService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.MysqlDump.MYSQL_DUMP.SERVER_ID.eq(server.getId()), 0, 50);
		return FacadeResult.doneExpectedResultDone(dumps);
	}

	// @formatter: off
	@ShellMethod(value = "添加或更改Mysql的描述")
	public FacadeResult<?> mysqlDescriptionUpdate(
			@ShowPossibleValue({"host", "port", "username", "password","flushLogCron"})
			@ShellOption(help = "需要改变的属性") @Pattern(regexp = "host|port|username|password|flushLogCron") String field,
			@ObjectFieldIndicator(objectClass=MysqlInstance.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureMysqlConfigurated();
		Server server = appState.currentServerOptional().get();
		MysqlInstance mi = server.getMysqlInstance();
		
		Optional<Field> fo = ObjectUtil.getField(MysqlInstance.class, field);
		Optional<Object> originOp = Optional.empty();
		try {
			if (fo.isPresent()) {
				originOp = Optional.ofNullable(fo.get().get(mi));
				ObjectUtil.setValue(fo.get(), mi, value);
				mi = mysqlInstanceService.save(mi);
				server.setMysqlInstance(mi);
			} else {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, field);
			}
		} catch (Exception e) {
			if (originOp.isPresent()) {
				try {
					fo.get().set(mi, originOp.get());
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
			return FacadeResult.unexpectedResult(e);
		}
		return FacadeResult.doneExpectedResultDone(mi);
	}

	@ShellMethod(value = "创建Mysql的描述")
	public FacadeResult<?> mysqlDescriptionCreate(
			@ShowDefaultValue @ShellOption(help = "mysql username.", defaultValue = "root") String username,
			@ShellOption(help = "mysql password.") String password) throws JSchException, IOException {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		MysqlInstance mi = server.getMysqlInstance();
		if (mi != null) {
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		}

		mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), password)
				.withFlushLogCron(dvs.getCron().getMysqlFlush()).withUsername(username).build();
		mi = mysqlInstanceService.save(mi);
		server.setMysqlInstance(mi);
		return FacadeResult.doneExpectedResultDone(mi);
	}

	@ShellMethod(value = "手动flush Mysql的日志")
	public FacadeResult<?> MysqlFlushLog() {
		sureMysqlReadyForBackup();
		Server server = appState.currentServerOptional().get();

		FacadeResult<String> fr = mysqlService.mysqlFlushLogs(getSession(), server);
		mysqlFlushService.processFlushResult(server, fr);
		return fr;
	}
	
	@ShellMethod(value = "列出flush Mysql的历史")
	public FacadeResult<?> MysqlFlushLogList() {
		sureMysqlReadyForBackup();
		Server server = appState.currentServerOptional().get();
		List<MysqlFlush> mfs = mysqlFlushService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.MysqlFlush.MYSQL_FLUSH.SERVER_ID.eq(server.getId()), 0, 50);
		return FacadeResult.doneExpectedResultDone(mfs);
	}


	@ShellMethod(value = "添加常用的CRON表达式")
	public FacadeResult<?> cronExpressionAdd(@ShellOption(help = "cron表达式") String expression,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		ReusableCron rc = new ReusableCron(expression, description);
		try {
			rc = reusableCronService.save(rc);
			return FacadeResult.doneExpectedResult(rc, CommonActionResult.DONE);
		} catch (Exception e) {
			return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, rc.getExpression());
		}
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
	public FacadeResult<?> userAdd(@ShellOption(help = "用户名") String name, @ShellOption(help = "email地址") String email,
			@ShellOption(help = "手机号码", defaultValue = ShellOption.NULL) String mobile,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		UserAccount ua = new UserAccount.UserAccountBuilder(name, email).withMobile(mobile).withDescription(description)
				.build();
		return FacadeResult.doneExpectedResult(userAccountService.save(ua), CommonActionResult.DONE);
	}

	@ShellMethod(value = "用户列表。")
	public FacadeResult<?> userList() {
		return FacadeResult.doneExpectedResult(userAccountService.findAll(), CommonActionResult.DONE);
	}

	private FacadeResult<?> parameterRequired(String pn) {
		if (!pn.startsWith("--")) {
			pn = "--" + pn;
		}
		return FacadeResult.showMessageExpected(CommonMessageKeys.PARAMETER_REQUIRED, pn);
	}

	private UserServerGrpVo getusgvo(UserServerGrp usgl) {
		return new UserServerGrpVo(usgl.getId(), userAccountService.findById(usgl.getUserAccountId()),
				serverGrpService.findById(usgl.getServerGrpId()), usgl.getCronExpression());
	}

	@ShellMethod(value = "列出用户和服务器组的关系。")
	public FacadeResult<?> userServerGroupList() {
		List<UserServerGrpVo> vos = userServerGrpService.findAll().stream().map(usgl -> getusgvo(usgl))
				.collect(Collectors.toList());

		return FacadeResult.doneExpectedResult(vos, CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "添加用户和服务器组的关系。")
	public FacadeResult<?> userServerGroupCreate(
			@ShellOption(help = "用户名") UserAccount user,
			@ShellOption(help = "服务器组") ServerGrp serverGroup,
			@ShellOption(help = "一个有意义的名称") String name,
			@CronStringIndicator @ShellOption(help = "任务计划") String cron) {
		UserServerGrp usg;
		if (user == null) {
			return parameterRequired("user");
		}
		if (serverGroup == null) {
			return parameterRequired("server-group");
		}
		usg = new UserServerGrp.UserServerGrpBuilder(user.getId(), serverGroup.getId(),ReusableCron.getExpressionFromToListRepresentation(cron))
				.withName(name)
				.build();
		usg = userServerGrpService.save(usg);
		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "删除用户和服务器组的关系。")
	public FacadeResult<?> userServerGroupDelete(
			@ShellOption(help = "要删除的User和ServerGrp关系。") UserServerGrp usg) {
		userServerGrpService.delete(usg);
		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
	}

	@ShellMethod(value = "添加服务器组。")
	public FacadeResult<?> ServerGroupAdd(@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
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
	public FacadeResult<?> ServerGroupMembers(@ShowPossibleValue({ "LIST", "ADD",
			"REMOVE" }) @ShellOption(help = "The action to take.") @Pattern(regexp = "ADD|REMOVE|LIST") String action,
			@ShellOption(help = "The server group to manage.") @NotNull ServerGrp serverGroup,
			@ShellOption(help = "The server to manage.", defaultValue = ShellOption.NULL) Server server) {
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
	public FacadeResult<?> userGroupAdd(@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
		UserGrp ug = new UserGrp(ename, msgkey);
		return null;
	}

	private String getPromptString() {
		switch (appState.getStep()) {
		case WAITING_SELECT:
			return "Please choose an instance by preceding number: ";
		default:
			if (appState.currentServerOptional().isPresent()) {
				return appState.currentServerOptional().get().getHost() + "> ";
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
	public List<String> schedulerJobList(@ShellOption(help = "列出全部而不单单是当前主机。") boolean all) throws SchedulerException {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		if (all) {
			return schedulerService.getAllSchedulerJobList();
		} else {
			return schedulerService.getBoxSchedulerJobList(server);
		}
	}

	@ShellMethod(value = "列出当前主机的计划任务触发器")
	public List<String> schedulerTriggerList() throws SchedulerException {
		sureBoxSelected();
		return schedulerService.getBoxTriggers(appState.currentServerOptional().get()).stream()
				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
	}

	@ShellMethod(value = "删除计划任务触发器")
	public FacadeResult<?> schedulerTriggerDelete(@ShellOption(help = "Trigger的名称。") String triggerKey) {
		sureBoxSelected();
		Server server = appState.currentServerOptional().get();
		return schedulerService.delteBoxTriggers(server, triggerKey);
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

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
