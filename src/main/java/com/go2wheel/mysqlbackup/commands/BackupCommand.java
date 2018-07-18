package com.go2wheel.mysqlbackup.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
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
import com.go2wheel.mysqlbackup.SecurityService;
import com.go2wheel.mysqlbackup.annotation.CandidatesFromSQL;
import com.go2wheel.mysqlbackup.annotation.CronStringIndicator;
import com.go2wheel.mysqlbackup.annotation.DbTableName;
import com.go2wheel.mysqlbackup.annotation.ObjectFieldIndicator;
import com.go2wheel.mysqlbackup.annotation.OstypeIndicator;
import com.go2wheel.mysqlbackup.annotation.SetServerOnly;
import com.go2wheel.mysqlbackup.annotation.ShowDefaultValue;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.annotation.TemplateIndicator;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;
import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserGrp;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.service.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlFlushDbService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.service.PlayBackService;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.service.ServerStateService;
import com.go2wheel.mysqlbackup.service.SqlService;
import com.go2wheel.mysqlbackup.service.StorageStateService;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
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
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.DefaultValues;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.UserServerGrpVo;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@ShellComponent()
public class BackupCommand {

	public static final String DESCRIPTION_FILENAME = "description.yml";

	public static final String DANGEROUS_ALERT = "I know what i am doing.";

	public static final int RESTART_CODE = 101;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SqlService sqlService;

	@Autowired
	private DefaultValues dvs;

	@Autowired
	private ServerStateService serverStateService;
	
	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Autowired
	private StorageStateService storageStateService;

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
	private UserAccountDbService userAccountDbService;

	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;

	@Autowired
	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private MysqlService mysqlService;

	private Session _session;

	@Autowired
	private BorgService borgService;

	@Autowired
	private MailerJob mailerJob;
	
	@Autowired
	private TemplateContextService templateContextService;

	@Autowired
	@Lazy
	private SchedulerService schedulerService;

	@Autowired
	private ServerGrpDbService serverGrpDbService;

	@Autowired
	private BorgDescriptionDbService borgDescriptionDbService;

	@Autowired
	private SubscribeDbService userServerGrpDbService;

	@Autowired
	private ReusableCronDbService reusableCronDbService;

	@Autowired
	private MySqlInstaller mySqlInstaller;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private MysqlFlushDbService mysqlFlushDbService;

	@Autowired
	private LocaledMessageService localedMessageService;

	@PostConstruct
	public void post() {

	}

	// @formatter:off
	private Session getSession() {
		sureServerSelected();
		Server server = appState.getCurrentServer();
		if (_session == null || !_session.isConnected()) {
			FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(server);
			if (frSession.isExpected()) {
				_session = frSession.getResult();
			} else {
				if (StringUtil.hasAnyNonBlankWord(frSession.getMessage())) {
					throw new UnExpectedInputException(null, frSession.getMessage(), "", frSession.getMessagePlaceHolders());
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
			@ShowDefaultValue() @ShellOption(help = "密码", defaultValue = Server.NO_PASSWORD) String password) {
		File sshk = ShellCommonParameterValue.NOT_EXIST_FILE.equals(sshKeyFile.getName()) ? null : sshKeyFile;
		File knonwh = ShellCommonParameterValue.NOT_EXIST_FILE.equals(knownHostsFile.getName()) ? null : knownHostsFile;
		String ppaw = Server.NO_PASSWORD.equals(password) ? null : password;

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
		return FacadeResult.doneExpectedResultDone(serverDbService.findAll());
	}

	@ShellMethod(value = "Pickup a server to work on.")
	public FacadeResult<?> serverSelect(@ShellOption(help = "服务器主机名或者IP") Server server) throws IOException {
		appState.setCurrentServer(server);
		return null;
	}

	@ShellMethod(value = "新建一个服务器.")
	public FacadeResult<?> serverCreate(
			@ShellOption(help = "服务器主机名或者IP") String host,
			@OstypeIndicator
			@ShellOption(help = "操作系统类型") String os,
			@ShowPossibleValue({"GET", "SET"})
			@ShellOption(help = "服务器的角色，默认是GET，从它那里获取数据。") String serverRole,
			@ShellOption(help = "服务器的名称") String name) throws IOException {
		Server server = serverDbService.findByHost(host);
		if (server == null) {
			server = new Server(host, name);
			server.setOs(os);
			server.setServerRole(serverRole);
			server.setServerStateCron(dvs.getCron().getServerState());
			server.setStorageStateCron(dvs.getCron().getStorageState());
			server = serverDbService.save(server);
		}
		return FacadeResult.doneExpectedResultDone(server);
	}
	
	@ShellMethod(value = "删除一个服务器.")
	public FacadeResult<?> serverDelete(
			@ShellOption(help = "服务器主机名或者IP") Server server,
			@ShellOption(defaultValue = "") String iknow) throws IOException {
		if (!DANGEROUS_ALERT.equals(iknow)) {
			return FacadeResult.unexpectedResult("mysql.dump.again.wrongprompt");
		}
		if (server == null) {
			return FacadeResult.showMessageExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, "");
		}
		serverDbService.delete(server);
		return FacadeResult.doneExpectedResult();
	}

	@ShellMethod(value = "显示服务器描述")
	public FacadeResult<?> serverDetail() throws JSchException, IOException {
		sureServerSelected();
		return FacadeResult.doneExpectedResult(appState.getCurrentServer(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "显示服务器健康度")
	public FacadeResult<?> serverHealthyState(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server
			) throws JSchException, IOException, RunRemoteCommandException {
		
		ServerAndSession sas = getServerAndSession(server);
		ServerState ss = serverStateService.createServerState(sas.getServer(), sas.getSession());
		return FacadeResult.doneExpectedResultDone(ss);
	}
	
	@ShellMethod(value = "显示服务器存储状态")
	public FacadeResult<?> serverStorageState(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server) throws JSchException, IOException, RunRemoteCommandException {
		
		ServerAndSession sas = getServerAndSession(server);
		List<StorageState> ssl = storageStateService.getStorageState(sas.getServer(), sas.getSession());
		return FacadeResult.doneExpectedResultDone(ssl);
	}

	@ShellMethod(value = "整理数据库中的关于存储状态的记录")
	public FacadeResult<?> serverStorageStatePrune(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server,
			@ShellOption(help = "保留指定天数内的记录", defaultValue="180") int keepDays,
			@ShellOption(help = "针对所有服务器。") boolean allServer) throws JSchException, IOException, RunRemoteCommandException {
		ServerAndSession sas = getServerAndSession(server, true);
		int deleted;
		
		if (allServer) {
			deleted = storageStateService.pruneStorageState(null, keepDays);
		} else {
			deleted = storageStateService.pruneStorageState(sas.getServer(), keepDays);
		}
		
		return FacadeResult.showMessageExpected(CommonMessageKeys.DB_RECORD_DELETED, deleted);
	}

	@ShellMethod(value = "获取CPU的核数")
	public FacadeResult<?> serverCoreNumber(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server) throws JSchException, IOException, RunRemoteCommandException {
		
		ServerAndSession sas = getServerAndSession(server);
		int i = serverStateService.getCoreNumber(server, sas.getSession());
		return FacadeResult.doneExpectedResultDone(i);

//		if (sas.getSession() != null) {
//			int i = SSHcommonUtil.coreNumber(sas.getSession());
//			return FacadeResult.doneExpectedResultDone(i);
//		} else {
//			return FacadeResult.showMessageUnExpected(CommonMessageKeys.UNSUPPORTED);
//		}
	}


	@ShellMethod(value = "和修改服务器描述")
	public FacadeResult<?> serverUpdate(
			@ShowPossibleValue({"host", "name" ,"port", "username", "password" ,"sshKeyFile", "serverRole", "uptimeCron", "diskfreeCron", "os"})
			@ShellOption(help = "需要改变的属性") String field,
			@ObjectFieldIndicator(objectClass=Server.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureServerSelected();
		Server server = appState.getCurrentServer();
		Optional<Field> fo = ObjectUtil.getField(Server.class, field);
		Optional<Object> originOp = Optional.empty();
		
		value = ObjectUtil.getValueWetherIsToListRepresentationOrNot(value, field);
		try {
			if (fo.isPresent()) {
				originOp = Optional.ofNullable(fo.get().get(server));
				ObjectUtil.setValue(fo.get(), server, value);
				server = serverDbService.save(server);
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

	@ShellMethod(value = "将数据库在目标服务器上重建。")
	public FacadeResult<?> serverAddDbPair(
			@SetServerOnly
			@ShellOption(help = "模拟的SET类型的服务器") Server server) throws IOException {
		
		return FacadeResult.doneExpectedResultDone(server);
	}
	
	@ShellMethod(value = "将文件目录在目标服务器上重建。")
	public FacadeResult<?> serverAddDirPair(
			@SetServerOnly
			@ShellOption(help = "模拟的SET类型的服务器") Server server,
			@ShellOption(help = "模拟路径") Server dir) throws IOException {
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
			boolean writed = uu.writeUpgradeFile();
			if (!writed) {
				UpgradeFile uf = uu.getUpgradeFile();
				if (uf != null) {
					return FacadeResult.showMessageExpected("command.upgrade.degrade", uf.getNewVersion(),
							uf.getCurrentVersion());
				} else {
					return FacadeResult.showMessageExpected("command.upgrade.degrade", "unknown",
							"unknown");
				}
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
		return mysqlService.enableLogbin(getSession(), appState.getCurrentServer(), logBinValue);
	}

	@ShellMethod(value = "查看logbin状态")
	public FacadeResult<?> mysqlGetLogbinState()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		sureMysqlConfigurated();
		return mysqlService.getLogbinState(getSession(), appState.getCurrentServer());
	}

	@ShellMethod(value = "查看myCnf")
	public FacadeResult<?> mysqlGetMycnf()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException, RunRemoteCommandException, ScpException {
		sureMysqlConfigurated();
		return mysqlService.getMyCnf(getSession(), appState.getCurrentServer());
	}

	@ShellMethod(value = "安装borg。")
	public FacadeResult<?> borgInstall() {
		sureBorgConfigurated();
		return borgService.install(getSession());
	}

	@ShellMethod(value = "创建Borg的描述")
	public FacadeResult<?> borgDescriptionCreate()
			throws JSchException, IOException {
		sureServerSelected();
		Server server = appState.getCurrentServer();
		BorgDescription bbd = server.getBorgDescription();
		if (bbd != null) {
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		}
		bbd = new BorgDescription.BorgDescriptionBuilder(server.getId()).withArchiveCron(dvs.getCron().getBorgArchive())
				.withPruneCron(dvs.getCron().getBorgPrune()).build();
		bbd = borgDescriptionDbService.save(bbd);
		server.setBorgDescription(bbd);
		return FacadeResult.doneExpectedResultDone(bbd);
	}
	
	
	@ShellMethod(value = "更新Borg的描述")
	public FacadeResult<?> borgDescriptionUpdate(
			@ShowPossibleValue({"repo", "archiveCron", "pruneCron", "includes","excludes"})
			@ShellOption(help = "需要改变的属性, 其中includes和exludes使用:符号分割") String field,
			@ObjectFieldIndicator(objectClass=BorgDescription.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
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
				bd = borgDescriptionDbService.save(bd);
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
			"80" }) @ShellOption(help = "两位数的版本号比如，55,56,57,80。") String twoDigitVersion,
			@ShellOption(help = "初始root的密码。") @Pattern(regexp = "[^\\s]{5,}") String initPassword) {
		sureServerSelected();
		Server server = appState.getCurrentServer();
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
		sureServerSelected();
		Server server = appState.getCurrentServer();
		return mySqlInstaller.unInstall(getSession(), server);
	}

	@ShellMethod(value = "初始化borg的repo。")
	public FacadeResult<?> borgRepoInit() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		return borgService.initRepo(getSession(), server.getBorgDescription().getRepo());
	}

	@ShellMethod(value = "创建一次borg备份")
	public FacadeResult<?> borgArchiveCreate(@ShellOption(help = "try to solve comman problems.") boolean solveProblems)
			throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		return borgService.archive(getSession(), server, solveProblems);
	}

	@ShellMethod(value = "下载borg的仓库。")
	public FacadeResult<?> borgRepoDownload() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		FacadeResult<BorgDownload> fr = borgService.downloadRepo(getSession(), server);
		BorgDownload bd = fr.getResult();
		bd.setTimeCost(fr.getEndTime() - fr.getStartTime());
		bd.setServerId(server.getId());
		bd = borgDownloadDbService.save(bd);
		fr.setResult(bd);
		return fr;
	}

	@ShellMethod(value = "列出borg创建的卷")
	public List<String> borgArchiveList() {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		return borgService.listArchives(getSession(), server).getResult().getArchives();
	}

	@ShellMethod(value = "修剪borg创建的卷")
	public String borgArchivePrune() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		BorgPruneResult bpr = borgService.pruneRepo(getSession(), server).getResult();
		return String.format("action: %s, pruned: %s, keeped: %s", bpr.isSuccess(), bpr.prunedArchiveNumbers(),
				bpr.keepedArchiveNumbers());
	}

	@ShellMethod(value = "列出borg仓库的文件，这些文件的意义由borg来解释。")
	public List<String> borgRepoListFiles() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Server server = appState.getCurrentServer();
		return borgService.listRepoFiles(getSession(), server).getResult().getAllTrimedNotEmptyLines();
	}

	private void sureServerSelected() {
		if (appState.getCurrentServer() == null) {
			throw new UnExpectedInputException(null, BackupCommandMsgKeys.SERVER_MISSING,
					"选择一个目标服务器先。 server-list, server-select.");
		}
	}

	private void sureBorgConfigurated() {
		sureServerSelected();
		if (appState.getCurrentServer().getBorgDescription() == null) {
			throw new UnExpectedInputException(null, "borg.unconfigurated", "");
		}
	}

	private void sureMysqlConfigurated() {
		sureServerSelected();
		if (appState.getCurrentServer().getMysqlInstance() == null) {
			throw new UnExpectedInputException(null, "mysql.unconfigurated", "");
		}
	}

	private void sureMysqlReadyForBackup() {
		sureMysqlConfigurated();
		Server server = appState.getCurrentServer();
		if (server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null) {
			throw new UnExpectedInputException(null, "mysql.unreadyforbackup", "", server.getHost());
		}
	}

	@ShellMethod(value = "执行Mysqldump命令")
	public FacadeResult<?> mysqlDump() throws JSchException, IOException {
		sureMysqlReadyForBackup();
		Server server = appState.getCurrentServer();
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(getSession(), server);
		return mysqlService.saveDumpResult(server, fr);
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
		Server server = appState.getCurrentServer();
		if (!DANGEROUS_ALERT.equals(iknow)) {
			return FacadeResult.unexpectedResult("mysql.dump.again.wrongprompt");
		}
		FacadeResult<LinuxLsl> fr = mysqlService.mysqlDump(getSession(), server, true);
		return mysqlService.saveDumpResult(server, fr);
	}



	
	@ShellMethod(value = "列出Mysqldump历史纪录")
	public FacadeResult<?> mysqlDumpList() throws JSchException, IOException {
		Server server = appState.getCurrentServer();
		List<MysqlDump> dumps = mysqlDumpDbService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.MysqlDump.MYSQL_DUMP.SERVER_ID.eq(server.getId()), 0, 50);
		return FacadeResult.doneExpectedResultDone(dumps);
	}

	// @formatter: off
	@ShellMethod(value = "添加或更改Mysql的描述")
	public FacadeResult<?> mysqlDescriptionUpdate(
			@ShowPossibleValue({"host", "port", "username", "password","flushLogCron", "dumpFileName", "clientBin"})
			@ShellOption(help = "需要改变的属性") String field,
			@ObjectFieldIndicator(objectClass=MysqlInstance.class)
			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
			) throws JSchException, IOException {
		sureMysqlConfigurated();
		Server server = appState.getCurrentServer();
		MysqlInstance mi = server.getMysqlInstance();
		
		Optional<Field> fo = ObjectUtil.getField(MysqlInstance.class, field);
		Optional<Object> originOp = Optional.empty();
		try {
			if (fo.isPresent()) {
				originOp = Optional.ofNullable(fo.get().get(mi));
				ObjectUtil.setValue(fo.get(), mi, value);
				mi = mysqlInstanceDbService.save(mi);
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
		sureServerSelected();
		Server server = appState.getCurrentServer();
		MysqlInstance mi = server.getMysqlInstance();
		if (mi != null) {
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		}

		mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), password)
				.withFlushLogCron(dvs.getCron().getMysqlFlush()).withUsername(username).build();
		mi = mysqlInstanceDbService.save(mi);
		server.setMysqlInstance(mi);
		return FacadeResult.doneExpectedResultDone(mi);
	}

	@ShellMethod(value = "手动flush Mysql的日志")
	public FacadeResult<?> MysqlFlushLog() {
		sureMysqlReadyForBackup();
		Server server = appState.getCurrentServer();

		FacadeResult<String> fr = mysqlService.mysqlFlushLogs(getSession(), server);
		mysqlFlushDbService.processFlushResult(server, fr);
		return fr;
	}
	
	@ShellMethod(value = "列出flush Mysql的历史")
	public FacadeResult<?> MysqlFlushLogList() {
		sureMysqlReadyForBackup();
		Server server = appState.getCurrentServer();
		List<MysqlFlush> mfs = mysqlFlushDbService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.MysqlFlush.MYSQL_FLUSH.SERVER_ID.eq(server.getId()), 0, 50);
		return FacadeResult.doneExpectedResultDone(mfs);
	}


	@ShellMethod(value = "添加常用的CRON表达式")
	public FacadeResult<?> cronExpressionAdd(@ShellOption(help = "cron表达式") String expression,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		ReusableCron rc = new ReusableCron(expression, description);
		try {
			rc = reusableCronDbService.save(rc);
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
		return FacadeResult.doneExpectedResult(reusableCronDbService.findAll(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "添加用户。")
	public FacadeResult<?> userAdd(@ShellOption(help = "用户名") String name, @ShellOption(help = "email地址") String email,
			@ShellOption(help = "手机号码", defaultValue = ShellOption.NULL) String mobile,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		UserAccount ua = new UserAccount.UserAccountBuilder(name, email).withMobile(mobile).withDescription(description)
				.build();
		return FacadeResult.doneExpectedResult(userAccountDbService.save(ua), CommonActionResult.DONE);
	}

	@ShellMethod(value = "用户列表。")
	public FacadeResult<?> userList() {
		return FacadeResult.doneExpectedResult(userAccountDbService.findAll(), CommonActionResult.DONE);
	}

	private FacadeResult<?> parameterRequired(String pn) {
		if (!pn.startsWith("--")) {
			pn = "--" + pn;
		}
		return FacadeResult.showMessageExpected(CommonMessageKeys.PARAMETER_REQUIRED, pn);
	}

	private UserServerGrpVo getusgvo(Subscribe usgl) {
		return new UserServerGrpVo(usgl.getId(), userAccountDbService.findById(usgl.getUserAccountId()),
				serverGrpDbService.findById(usgl.getServerGrpId()), usgl.getCronExpression());
	}

	@ShellMethod(value = "列出用户和服务器组的关系。")
	public FacadeResult<?> subscribeList() {
		List<UserServerGrpVo> vos = userServerGrpDbService.findAll().stream().map(usgl -> getusgvo(usgl))
				.collect(Collectors.toList());

		return FacadeResult.doneExpectedResult(vos, CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "添加用户和服务器组的关系。")
	public FacadeResult<?> subscribeCreate(
			@ShellOption(help = "用户名") UserAccount user,
			@ShellOption(help = "服务器组") ServerGrp serverGroup,
			@ShellOption(help = "一个有意义的名称") String name,
			@TemplateIndicator
			@ShellOption(help = "邮件的模板名称") String template,
			@CronStringIndicator @ShellOption(help = "任务计划") String cron) {
		Subscribe usg;
		if (user == null) {
			return parameterRequired("user");
		}
		if (serverGroup == null) {
			return parameterRequired("server-group");
		}
		usg = new Subscribe.SubscribeBuilder(user.getId(), serverGroup.getId(),ReusableCron.getExpressionFromToListRepresentation(cron), name)
				.withTemplate(template)
				.build();
		usg = userServerGrpDbService.save(usg);
		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "删除用户和服务器组的关系。")
	public FacadeResult<?> subscribeDelete(
			@ShellOption(help = "要删除的User和ServerGrp关系。") Subscribe usg) {
		userServerGrpDbService.delete(usg);
		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
	}

	@ShellMethod(value = "添加服务器组。")
	public FacadeResult<?> ServerGroupAdd(@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
		ServerGrp sg = new ServerGrp(ename);
		sg.setMsgkey(msgkey);
		sg = serverGrpDbService.save(sg);
		return FacadeResult.doneExpectedResult(sg, CommonActionResult.DONE);
	}

	@ShellMethod(value = "列出服务器组。")
	public FacadeResult<?> ServerGroupList() {
		List<ServerGrp> sgs = serverGrpDbService.findAll();
		return FacadeResult.doneExpectedResult(sgs, CommonActionResult.DONE);
	}

	@ShellMethod(value = "管理服务器组的主机")
	public FacadeResult<?> ServerGroupMembers(@ShowPossibleValue({ "LIST", "ADD",
			"REMOVE" }) @ShellOption(help = "The action to take.") String action,
			@ShellOption(help = "The server group to manage.") @NotNull ServerGrp serverGroup,
			@ShellOption(help = "The server to manage.", defaultValue = ShellOption.NULL) Server server) {
		switch (action) {
		case "ADD":
			if (server == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
			}
			serverGrpDbService.addServer(serverGroup, server);
			break;
		case "REMOVE":
			if (server == null) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
			}
			serverGrpDbService.removeServer(serverGroup, server);
			break;
		default:
			break;
		}
		return FacadeResult.doneExpectedResult(serverGrpDbService.getServers(serverGroup), CommonActionResult.DONE);
	}

	@ShellMethod(value = "添加用户组。")
	public FacadeResult<?> userGroupAdd(@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
		UserGrp ug = new UserGrp(ename, msgkey);
		return FacadeResult.doneExpectedResultDone(ug);
	}

	private String getPromptString() {
		switch (appState.getStep()) {
		case WAITING_SELECT:
			return "Please choose an instance by preceding number: ";
		default:
			Server server = appState.getCurrentServer();
			if (server != null) {
				return server.getHost() + "> ";
			} else {
				return "no_server_selected> ";
			}
		}
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
	public FacadeResult<?> schedulerJobList(@ShellOption(help = "列出全部而不单单是当前主机。") boolean all) throws SchedulerException {
		sureServerSelected();
		Server server = appState.getCurrentServer();
		if (all) {
			return FacadeResult.doneExpectedResultDone(schedulerService.getAllJobKeys().stream().map(StringUtil::formatJobkey).collect(Collectors.toList()));
		} else {
			return FacadeResult.doneExpectedResultDone(schedulerService.getJobkeysOfServer(server).stream().map(StringUtil::formatJobkey).collect(Collectors.toList()));
		}
	}
	
	
	@ShellMethod(value = "删除JOB")
	public FacadeResult<?> schedulerJobDelete(@ShellOption(help = "job key。") JobKey jobKey) throws SchedulerException {
		sureServerSelected();
		schedulerService.getDeleteJob(jobKey);
		return FacadeResult.doneExpectedResult();
	}
	
	@ShellMethod(value = "重新设置出发器")
	public void schedulerRescheduleJob(String triggerKey, String cronExpression)
			throws SchedulerException, ParseException {
		sureServerSelected();
		schedulerService.schedulerRescheduleJob(triggerKey, cronExpression);
	}



	@ShellMethod(value = "列出当前主机的计划任务触发器")
	public List<String> schedulerTriggerList() throws SchedulerException {
		sureServerSelected();
		return schedulerService.getServerTriggers(appState.getCurrentServer()).stream()
				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
	}

	@ShellMethod(value = "删除计划任务触发器")
	public FacadeResult<?> schedulerTriggerDelete(@ShellOption(help = "Trigger的名称。") TriggerKey triggerKey) {
		sureServerSelected();
		return schedulerService.delteBoxTriggers(triggerKey);
	}
	
	
	@ShellMethod(value = "创建模板的Context数据")
	public FacadeResult<?> extraTemplateContext(
			@ShellOption(help = "userServerGrp的ID值，可通过user-server-group-list命令查看。") int userServerGrpId,
			@ShellOption(help = "输出文件的名称。", defaultValue=ShellOption.NULL) String outfile
			) throws IOException {
		ServerGroupContext sgc = templateContextService.createMailerContext(userServerGrpId);
		Path pa = Paths.get("templates", "tplcontext.yml");
		if (outfile != null) {
			pa = Paths.get("templates", outfile); 
		}
		String s = YamlInstance.INSTANCE.yaml.dumpAsMap(sgc);
		Files.write(pa, s.getBytes(StandardCharsets.UTF_8));
		return FacadeResult.doneExpectedResult();
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
	
	@ShellMethod(value = "执行SQL SELECT.")
	public FacadeResult<?> sqlSelect(
			@DbTableName
			@ShellOption(help = "数据表名称") String tableName,
			@ShellOption(help = "返回的记录数", defaultValue="10") int limit) {
		return FacadeResult.doneExpectedResultDone(sqlService.select(tableName, limit));
	}
	
	@ShellMethod(value = "执行SQL DELETE.")
	public FacadeResult<?> sqlDelete(
			@DbTableName
			@ShellOption(help = "数据表名称") String tableName,
			@ShellOption(help = "记录的ID") int id) {
		return FacadeResult.doneExpectedResultDone(sqlService.delete(tableName, id));
	}
	
	@ShellMethod(value = "执行远程命令.")
	public FacadeResult<?> testRunRemote(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server,
			@ShellOption(help = "command to run.") String command
			) throws RunRemoteCommandException {
		ServerAndSession sas = getServerAndSession(server);
		if (sas.getSession() != null) {
			return FacadeResult.doneExpectedResultDone(SSHcommonUtil.runRemoteCommand(sas.getSession(), command));
		}
		return FacadeResult.unexpectedResult(CommonMessageKeys.UNSUPPORTED);
	}

	
	@ShellMethod(value = "获取HSQLDB的CRYPT_KEY")
	public FacadeResult<?> securityKeygen(@ShellOption(help = "编码方式", defaultValue="AES") String enc) throws ClassNotFoundException, SQLException {
		return securityService.securityKeygen(enc);
	}
	
	@ShellMethod(value = "将SSHkey从文件复制到数据库或反之。")
	public FacadeResult<?> securityCopySshkey(@ShellOption(help = "db到文件") boolean toFile,
			@ShellOption(help = "删除ssh文件") boolean deleteFile) throws ClassNotFoundException, IOException {
		return securityService.securityCopySshkey(toFile, deleteFile);
	}
	
	@ShellMethod(value = "将KnownHosts从文件复制到数据库或反之。")
	public FacadeResult<?> securityCopyKnownHosts(@ShellOption(help = "db到文件") boolean toFile) throws ClassNotFoundException, IOException {
		return securityService.securityCopyKnownHosts(toFile);
	}

	@ShellMethod(value = "立即发送建邮件通知。")
	public FacadeResult<?> emailNoticeSend(
			@ShellOption(help = "邮件地址") @Email String email,
			@TemplateIndicator
			@ShellOption(help = "邮件模板") String template,
			@ShellOption(help = "用户服务器组") Subscribe userServerGrp,
			@ShellOption(help = "真的发送") boolean sendTruely
			) throws ClassNotFoundException, IOException {
		ServerGroupContext sgctx = templateContextService.createMailerContext(userServerGrp);
		if (sendTruely) {
			mailerJob.mail(email, template, sgctx);
			return FacadeResult.doneExpectedResultDone("mail had sent to " + email + ".");
		} else {
			return FacadeResult.doneExpectedResultDone(mailerJob.renderTemplate(template, sgctx));
		}
	}
	
	@Autowired
	private PlayBackService playBackService;
	
	@ShellMethod(value = "创建回放设定。")
	public FacadeResult<?> playbackCreate(
			@ShellOption(help = "源服务器") Server sourceServer,
			@ShellOption(help = "回放服务器") Server targetServer,
			@ShowPossibleValue({PlayBack.PLAY_BORG, PlayBack.PLAY_MYSQL})
			@ShellOption(help = "回放内容") String playWhat,
			@ShellOption(help = "设定条目", defaultValue=ShellOption.NULL) List<String> settings
			) {
		PlayBack pb = playBackService.create(sourceServer, targetServer, playWhat, settings);
		return FacadeResult.doneExpectedResultDone(pb);
	}
	
	@ShellMethod(value = "删除回放设定。")
	public FacadeResult<?> playbackDelete(
			@ShellOption(help = "回放设定") PlayBack playback) {
		playBackService.remove(playback);
		return FacadeResult.doneExpectedResult();
	}
	
	@Autowired
	private KeyValueDbService keyValueDbService;
	
	public static final String KEY_VALUE_CANDIDATES_SQL = "SELECT ITEM_KEY FROM KEY_VALUE WHERE ITEM_KEY LIKE '%%%s%%'";
	
	@ShellMethod(value = "查询键值对。")
	public FacadeResult<?> keyValueGet(
			@CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL)
			@ShellOption(help = "键值，可以用dot分隔") String key
			) {
		List<KeyValue> kvs = keyValueDbService.findByKeyPrefix(key);
		return FacadeResult.doneExpectedResultDone(kvs);
	}
	
	@ShellMethod(value = "删除键值对。")
	public FacadeResult<?> keyValueDelete(
			@CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL)
			@ShellOption(help = "键值，可以用dot分隔") String key
			) {
		KeyValue kv = keyValueDbService.findOneByKey(key);
		if (kv == null) {
			return FacadeResult.doneExpectedResultPreviousDone(CommonMessageKeys.DB_ITEMNOTEXISTS);
		} else {
			keyValueDbService.delete(kv);
			return FacadeResult.doneExpectedResultDone(kv);
		}
		
	}

	
	@ShellMethod(value = "新建或更新键值对。")
	public FacadeResult<?> keyValueUpdateOrCreate(
			@CandidatesFromSQL(KEY_VALUE_CANDIDATES_SQL)
			@ShellOption(help = "键值，可以用dot分隔") String key,
			@ShellOption(help = "值") String value
			) {
		KeyValue kv = keyValueDbService.findOneByKey(key);
		if (kv != null) {
			if (!kv.getItemValue().equals(value)) {
				kv.setItemValue(value);
				kv = keyValueDbService.save(kv);
			}
		} else {
			kv = new KeyValue(key, value);
			kv = keyValueDbService.save(kv);
		}
		return FacadeResult.doneExpectedResultDone(kv);
	}

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}
	
	private ServerAndSession getServerAndSession(Server server) {
		return getServerAndSession(server, false);
	}
	
	private ServerAndSession getServerAndSession(Server server, boolean notConnect) {
		if (server == null) {
			sureServerSelected();
			server = appState.getCurrentServer();
		}
		Session sess = null;
		if (!notConnect) {
			if (server.supportSSH()) {
				if (server.getId().equals(appState.getCurrentServer().getId())) {
					sess = getSession();
				} else {
					sess = sshSessionFactory.getConnectedSession(server).getResult();
				}
			}
		}
		return new ServerAndSession(server, sess);
	}
	
	private class ServerAndSession {
		private final Server server;
		private final Session session;
		
		public ServerAndSession(Server server, Session session) {
			this.server = server;
			this.session = session;
		}

		public Server getServer() {
			return server;
		}

		public Session getSession() {
			return session;
		}
	}
}
