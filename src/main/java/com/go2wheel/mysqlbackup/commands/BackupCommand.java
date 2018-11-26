package com.go2wheel.mysqlbackup.commands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.validation.constraints.Email;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.go2wheel.mysqlbackup.AppEventListenerBean;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.SecurityService;
import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.annotation.CandidatesFromSQL;
import com.go2wheel.mysqlbackup.annotation.DbTableName;
import com.go2wheel.mysqlbackup.annotation.SetServerOnly;
import com.go2wheel.mysqlbackup.annotation.ShowPossibleValue;
import com.go2wheel.mysqlbackup.annotation.TemplateIndicator;
import com.go2wheel.mysqlbackup.dbservice.BorgDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore;
import com.go2wheel.mysqlbackup.dbservice.GlobalStore.SavedFuture;
import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.dbservice.PlayBackService;
import com.go2wheel.mysqlbackup.dbservice.ReusableCronDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.dbservice.SqlService;
import com.go2wheel.mysqlbackup.dbservice.TemplateContextService;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;
import com.go2wheel.mysqlbackup.job.MailerJob;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.mail.ServerGroupContext;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.model.UserGrp;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@ShellComponent()
public class BackupCommand {

	public static final String DESCRIPTION_FILENAME = "description.yml";

	public static final String DANGEROUS_ALERT = "I know what i am doing.";

	public static final int RESTART_CODE = 101;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GlobalStore globalStore;

	@Autowired
	private SqlService sqlService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SettingsInDb settingsInDb;

	@Autowired
	private Environment environment;

	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;

	@Autowired
	private MailerJob mailerJob;

	@Autowired
	private TemplateContextService templateContextService;

	@Autowired
	@Lazy
	private SchedulerService schedulerService;

	@Autowired
	private BorgDescriptionDbService borgDescriptionDbService;

	@Autowired
	private ReusableCronDbService reusableCronDbService;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private LocaledMessageService localedMessageService;
	
	@Autowired
	private AppEventListenerBean appEventListenerBean;

	@PostConstruct
	public void post() {

	}


	@ShellMethod(value = "List all managed servers.")
	public FacadeResult<?> serverList() throws IOException {
		return FacadeResult.doneExpectedResultDone(serverDbService.findAll());
	}

//	@ShellMethod(value = "Pickup a server to work on.")
//	public FacadeResult<?> serverSelect(@ShellOption(help = "服务器主机名或者IP") Server server) throws IOException {
//		appState.setCurrentServer(server);
//		return null;
//	}

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

//	@ShellMethod(value = "显示服务器描述")
//	public FacadeResult<?> serverDetail() throws  IOException, UnExpectedInputException {
//		sureServerSelected();
//		return FacadeResult.doneExpectedResult(appState.getCurrentServer(), CommonActionResult.DONE);
//	}

	@ShellMethod(value = "显示服务器健康度")
	public FacadeResult<?> serverHealthyState(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server
			) throws  IOException, RunRemoteCommandException, UnExpectedInputException, UnExpectedOutputException {
		
//		ServerState ss = serverStateService.createServerState(sas.getServer(), sas.getSession());
//		return FacadeResult.doneExpectedResultDone(ss);
		return null;
	}
	
	@ShellMethod(value = "显示服务器存储状态")
	public FacadeResult<?> serverStorageState(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server) throws  IOException, RunRemoteCommandException, UnExpectedInputException {
//		List<StorageState> ssl = storageStateService.getStorageState(sas.getServer(), sas.getSession());
//		return FacadeResult.doneExpectedResultDone(ssl);
		return null;
	}

//	@ShellMethod(value = "整理数据库中的关于存储状态的记录")
//	public FacadeResult<?> serverStorageStatePrune(
//			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server,
//			@ShellOption(help = "保留指定天数内的记录", defaultValue="180") int keepDays,
//			@ShellOption(help = "针对所有服务器。") boolean allServer) throws  IOException, RunRemoteCommandException, UnExpectedInputException {
//		int deleted;
//		
//		if (allServer) {
//			deleted = storageStateService.pruneStorageState(null, keepDays);
//		} else {
//			deleted = storageStateService.pruneStorageState(sas.getServer(), keepDays);
//		}
//		
//		return FacadeResult.showMessageExpected(CommonMessageKeys.DB_RECORD_DELETED, deleted);
//	}

	@ShellMethod(value = "获取CPU的核数")
	public FacadeResult<?> serverCoreNumber(
			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server) throws  IOException, RunRemoteCommandException, UnExpectedInputException {
		
//		int i = serverStateService.getCoreNumber(server, sas.getSession());
//		return FacadeResult.doneExpectedResultDone(i);
		return null;
	}


//	@ShellMethod(value = "和修改服务器描述")
//	public FacadeResult<?> serverUpdate(
//			@ShowPossibleValue({"host", "name" ,"port", "username", "password" ,"sshKeyFile", "serverRole", "uptimeCron", "diskfreeCron", "os"})
//			@ShellOption(help = "需要改变的属性") String field,
//			@ObjectFieldIndicator(objectClass=Server.class)
//			@ShellOption(help = "新的值", defaultValue=ShellOption.NULL) String value
//			) throws  IOException, UnExpectedInputException {
//		sureServerSelected();
//		Server server = appState.getCurrentServer();
//		Optional<Field> fo = ObjectUtil.getField(Server.class, field);
//		Optional<Object> originOp = Optional.empty();
//		
//		value = ObjectUtil.getValueWetherIsToListRepresentationOrNot(value, field);
//		try {
//			if (fo.isPresent()) {
//				originOp = Optional.ofNullable(fo.get().get(server));
//				ObjectUtil.setValue(fo.get(), server, value);
//				server = serverDbService.save(server);
//			} else {
//				return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_NOT_EXISTS, field);
//			}
//		} catch (Exception e) {
//			if (originOp.isPresent()) {
//				try {
//					fo.get().set(server, originOp.get());
//				} catch (IllegalArgumentException | IllegalAccessException e1) {
//					e1.printStackTrace();
//				}
//			}
//			return FacadeResult.unexpectedResult(e);
//		}
//		return FacadeResult.doneExpectedResultDone(server);
//	}

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
				formatKeyVal("server profile dirctory", settingsInDb.getDataDir().toAbsolutePath().toString()),
				formatKeyVal("database url", environment.getProperty("spring.datasource.url")),
				formatKeyVal("working directory", Paths.get("").toAbsolutePath().normalize().toString()),
				formatKeyVal("download directory",
						settingsInDb.getDownloadPath().normalize().toAbsolutePath().toString()),
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
	public FacadeResult<?> systemUpgrade(@ShellOption(help = "新版本的zip文件") File zipFile) throws UnExpectedInputException {
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


	/**
	 * 1. check if already initialized. 2. get my.cnf content 3. check if
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSchException
	 * @throws UnExpectedInputException 
	 * @throws UnExpectedOutputException 
	 * @throws MysqlAccessDeniedException 
	 * @throws CommandNotFoundException 
	 */
//	@ShellMethod(value = "为备份MYSQL作准备。")
//	public FacadeResult<?> mysqlEnableLogbin(
//			@ShowDefaultValue @ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。", defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME) String logBinValue)
//			throws  IOException, UnExpectedInputException, UnExpectedOutputException, MysqlAccessDeniedException, CommandNotFoundException {
//		sureMysqlConfigurated();
//		return mysqlService.enableLogbin(getSession(), appState.getCurrentServer(), logBinValue);
//	}
//
//	@ShellMethod(value = "查看logbin状态")
//	public FacadeResult<?> mysqlGetLogbinState()
//			throws  IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, UnExpectedOutputException, CommandNotFoundException {
//		sureMysqlConfigurated();
//		return mysqlService.getLogbinState(getSession(), appState.getCurrentServer());
//	}
//
//	@ShellMethod(value = "查看myCnf")
//	public FacadeResult<?> mysqlGetMycnf()
//			throws  IOException, MysqlAccessDeniedException, AppNotStartedException, RunRemoteCommandException, ScpException, UnExpectedInputException, UnExpectedOutputException {
//		sureMysqlConfigurated();
//		return mysqlService.getMyCnf(getSession(), appState.getCurrentServer());
//	}
//
//	@ShellMethod(value = "安装borg。")
//	public FacadeResult<?> borgInstall(@MetaAnno("BORG") Software software) throws UnExpectedInputException, JSchException {
//		sureBorgConfigurated();
//		Server server = appState.getCurrentServer();
//		return borgInstaller.install(getSession(), server, software, null);
//	}


	






	@ShellMethod(value = "列出后台任务")
	public FacadeResult<?> asyncList() throws  IOException {
		List<SavedFuture> gobjects = globalStore.getFutureGroupAll(BackupCommand.class.getName());
		
		List<String> ls =  gobjects.stream()
				.map(sf -> String.format("Task %s, Done: %s",sf.getDescription(), sf.getCf().isDone()))
				.collect(Collectors.toList());
		return FacadeResult.doneExpectedResultDone(ls);
		
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

//	@ShellMethod(value = "添加用户。")
//	public FacadeResult<?> userAdd(@ShellOption(help = "用户名") String name, @ShellOption(help = "email地址") String email,
//			@ShellOption(help = "手机号码", defaultValue = ShellOption.NULL) String mobile,
//			@ShellOption(help = "描述", defaultValue = "") String description) {
//		UserAccount ua = new UserAccount.UserAccountBuilder(name, email).withMobile(mobile).withDescription(description)
//				.build();
//		return FacadeResult.doneExpectedResult(userAccountDbService.save(ua), CommonActionResult.DONE);
//	}
//
//	@ShellMethod(value = "用户列表。")
//	public FacadeResult<?> userList() {
//		return FacadeResult.doneExpectedResult(userAccountDbService.findAll(), CommonActionResult.DONE);
//	}

	private FacadeResult<?> parameterRequired(String pn) {
		if (!pn.startsWith("--")) {
			pn = "--" + pn;
		}
		return FacadeResult.showMessageExpected(CommonMessageKeys.PARAMETER_REQUIRED, pn);
	}

//	private UserServerGrpVo getusgvo(Subscribe usgl) {
//		return new UserServerGrpVo(usgl.getId(), userAccountDbService.findById(usgl.getUserAccountId()),
//				serverGrpDbService.findById(usgl.getServerGrpId()), usgl.getCronExpression());
//	}

//	@ShellMethod(value = "列出用户和服务器组的关系。")
//	public FacadeResult<?> subscribeList() {
//		List<UserServerGrpVo> vos = userServerGrpDbService.findAll().stream().map(usgl -> getusgvo(usgl))
//				.collect(Collectors.toList());
//
//		return FacadeResult.doneExpectedResult(vos, CommonActionResult.DONE);
//	}
	
//	@ShellMethod(value = "添加用户和服务器组的关系。")
//	public FacadeResult<?> subscribeCreate(
//			@ShellOption(help = "用户名") UserAccount user,
//			@ShellOption(help = "服务器组") ServerGrp serverGroup,
//			@ShellOption(help = "一个有意义的名称") String name,
//			@TemplateIndicator
//			@ShellOption(help = "邮件的模板名称") String template,
//			@CronStringIndicator @ShellOption(help = "任务计划") String cron) {
//		Subscribe usg;
//		if (user == null) {
//			return parameterRequired("user");
//		}
//		if (serverGroup == null) {
//			return parameterRequired("server-group");
//		}
//		usg = new Subscribe.SubscribeBuilder(user.getId(), serverGroup.getId(),ReusableCron.getExpressionFromToListRepresentation(cron), name)
//				.withTemplate(template)
//				.build();
//		usg = userServerGrpDbService.save(usg);
//		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
//	}
	
//	@ShellMethod(value = "删除用户和服务器组的关系。")
//	public FacadeResult<?> subscribeDelete(
//			@ShellOption(help = "要删除的User和ServerGrp关系。") Subscribe usg) {
//		userServerGrpDbService.delete(usg);
//		return FacadeResult.doneExpectedResult(getusgvo(usg), CommonActionResult.DONE);
//	}

//	@ShellMethod(value = "添加服务器组。")
//	public FacadeResult<?> ServerGroupAdd(@ShellOption(help = "组的英文名称") String ename,
//			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
//		ServerGrp sg = new ServerGrp(ename);
//		sg.setMsgkey(msgkey);
//		sg = serverGrpDbService.save(sg);
//		return FacadeResult.doneExpectedResult(sg, CommonActionResult.DONE);
//	}

//	@ShellMethod(value = "列出服务器组。")
//	public FacadeResult<?> ServerGroupList() {
//		List<ServerGrp> sgs = serverGrpDbService.findAll();
//		return FacadeResult.doneExpectedResult(sgs, CommonActionResult.DONE);
//	}

//	@ShellMethod(value = "管理服务器组的主机")
//	public FacadeResult<?> ServerGroupMembers(@ShowPossibleValue({ "LIST", "ADD",
//			"REMOVE" }) @ShellOption(help = "The action to take.") String action,
//			@ShellOption(help = "The server group to manage.") @NotNull ServerGrp serverGroup,
//			@ShellOption(help = "The server to manage.", defaultValue = ShellOption.NULL) Server server) {
//		switch (action) {
//		case "ADD":
//			if (server == null) {
//				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
//			}
//			serverGrpDbService.addServer(serverGroup, server);
//			break;
//		case "REMOVE":
//			if (server == null) {
//				return FacadeResult.showMessageUnExpected(CommonMessageKeys.PARAMETER_REQUIRED, "--server");
//			}
//			serverGrpDbService.removeServer(serverGroup, server);
//			break;
//		default:
//			break;
//		}
//		return FacadeResult.doneExpectedResult(serverGrpDbService.getServers(serverGroup), CommonActionResult.DONE);
//	}

	@ShellMethod(value = "添加用户组。")
	public FacadeResult<?> userGroupAdd(@ShellOption(help = "组的英文名称") String ename,
			@ShellOption(help = "message的键值，如果需要国际化的话", defaultValue = ShellOption.NULL) String msgkey) {
		UserGrp ug = new UserGrp(ename, msgkey);
		return FacadeResult.doneExpectedResultDone(ug);
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
		appEventListenerBean.setLocal(l);
		return "switch to language: " + language;
	}

//	@ShellMethod(value = "列出当前主机的计划任务")
//	public FacadeResult<?> schedulerJobList(@ShellOption(help = "列出全部而不单单是当前主机。") boolean all) throws SchedulerException, UnExpectedInputException {
//		Server server = appState.getCurrentServer();
//		if (all) {
//			return FacadeResult.doneExpectedResultDone(schedulerService.getAllJobKeys().stream().map(StringUtil::formatJobkey).collect(Collectors.toList()));
//		} else {
//			return FacadeResult.doneExpectedResultDone(schedulerService.getJobkeysOfServer(server).stream().map(StringUtil::formatJobkey).collect(Collectors.toList()));
//		}
//	}
	
//	
//	@ShellMethod(value = "删除JOB")
//	public FacadeResult<?> schedulerJobDelete(@ShellOption(help = "job key。") JobKey jobKey) throws SchedulerException, UnExpectedInputException {
//		sureServerSelected();
//		schedulerService.getDeleteJob(jobKey);
//		return FacadeResult.doneExpectedResult();
//	}
//	
//	@ShellMethod(value = "重新设置出发器")
//	public void schedulerRescheduleJob(String triggerKey, String cronExpression)
//			throws SchedulerException, ParseException, UnExpectedInputException {
//		sureServerSelected();
//		schedulerService.schedulerRescheduleJob(triggerKey, cronExpression);
//	}



//	@ShellMethod(value = "列出当前主机的计划任务触发器")
//	public List<String> schedulerTriggerList() throws SchedulerException, UnExpectedInputException {
//		sureServerSelected();
//		return schedulerService.getServerTriggers(appState.getCurrentServer()).stream()
//				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
//	}
//
//	@ShellMethod(value = "删除计划任务触发器")
//	public FacadeResult<?> schedulerTriggerDelete(@ShellOption(help = "Trigger的名称。") TriggerKey triggerKey) throws UnExpectedInputException {
//		sureServerSelected();
//		return schedulerService.delteBoxTriggers(triggerKey);
//	}
	
	
//	@ShellMethod(value = "创建模板的Context数据")
//	public FacadeResult<?> extraTemplateContext(
//			@ShellOption(help = "userServerGrp的ID值，可通过user-server-group-list命令查看。") int userServerGrpId,
//			@ShellOption(help = "输出文件的名称。", defaultValue=ShellOption.NULL) String outfile
//			) throws IOException {
//		ServerGroupContext sgc = templateContextService.createMailerContext(userServerGrpId);
//		Path pa = Paths.get("templates", "tplcontext.yml");
//		if (outfile != null) {
//			pa = Paths.get("templates", outfile); 
//		}
//		String s = YamlInstance.INSTANCE.yaml.dumpAsMap(sgc);
//		Files.write(pa, s.getBytes(StandardCharsets.UTF_8));
//		return FacadeResult.doneExpectedResult();
//	}

//	@ShellMethod(value = "查看最后一个命令的详细执行结果")
//	public String facadeResultLast() {
//		FacadeResult<?> fr = appEventListenerBean.getFacadeResult();
//		if (fr == null) {
//			return "";
//		} else {
//			if (fr.getException() != null) {
//				return ExceptionUtil.stackTraceToString(fr.getException());
//			} else if (fr.getResult() != null) {
//				return fr.getResult().toString();
//			} else if (fr.getMessage() != null && !fr.getMessage().isEmpty()) {
//				return localedMessageService.getMessage(fr.getMessage());
//			} else {
//				return "";
//			}
//		}
//	}
	
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
	
//	@ShellMethod(value = "执行远程命令.")
//	public FacadeResult<?> testRunRemote(
//			@ShellOption(help = "目标服务器", defaultValue=ShellOption.NULL) Server server,
//			@ShellOption(help = "command to run.") String command
//			) throws RunRemoteCommandException, UnExpectedInputException,  IOException {
//		ServerAndSession sas = null;
//		try {
//			sas = getServerAndSession(server);
//		} catch (JSchException e) {
//			return Exception2FacadeResult.parseException(e);
//		}
//		if (sas != null && sas.getSession() != null) {
//			return FacadeResult.doneExpectedResultDone(SSHcommonUtil.runRemoteCommand(sas.getSession(), command));
//		}
//		return FacadeResult.unexpectedResult(CommonMessageKeys.UNSUPPORTED);
//	}

	
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

//	@ShellMethod(value = "立即发送建邮件通知。")
//	public FacadeResult<?> emailNoticeSend(
//			@ShellOption(help = "邮件地址") @Email String email,
//			@TemplateIndicator
//			@ShellOption(help = "邮件模板") String template,
//			@ShellOption(help = "用户服务器组") Subscribe subscribe,
//			@ShellOption(help = "真的发送") boolean sendTruely
//			) throws ClassNotFoundException, IOException, MessagingException {
//		ServerGroupContext sgctx = templateContextService.createMailerContext(subscribe);
//		if (sendTruely) {
//			mailerJob.mail(subscribe, email, template, sgctx);
//			return FacadeResult.doneExpectedResultDone("mail had sent to " + email + ".");
//		} else {
//			return FacadeResult.doneExpectedResultDone(mailerJob.renderTemplate(template, sgctx));
//		}
//	}
	
	@Autowired
	private PlayBackService playBackService;
	
	@ShellMethod(value = "创建回放设定。")
	public FacadeResult<?> playbackCreate(
			@ShellOption(help = "源服务器") Server sourceServer,
			@ShellOption(help = "回放服务器") Server targetServer,
			@ShowPossibleValue({PlayBack.PLAY_BORG, PlayBack.PLAY_MYSQL})
			@ShellOption(help = "回放内容") String playWhat,
			@ShellOption(help = "设定条目", defaultValue=ShellOption.NULL) List<String> settings
			) throws UnExpectedInputException {
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
	
	private String getPromptString() {
//	switch (appState.getStep()) {
//	case WAITING_SELECT:
//		return "Please choose an instance by preceding number: ";
//	default:
//		Server server = appState.getCurrentServer();
//		if (server != null) {
//			return server.getHost() + "> ";
//		} else {
			return "no_server_selected> ";
//		}
	}
}
