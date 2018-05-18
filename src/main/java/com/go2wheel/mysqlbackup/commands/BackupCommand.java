package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Pattern;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.ApplicationState.CommandStepState;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.exception.NoServerSelectedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ServerConnectionException;
import com.go2wheel.mysqlbackup.exception.ShowToUserException;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder;
import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;
import com.go2wheel.mysqlbackup.job.SchedulerService;
import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.service.MailAddressService;
import com.go2wheel.mysqlbackup.service.ReusableCronService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.ToStringFormat;
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@ShellComponent()
public class BackupCommand {

	public static final String DESCRIPTION_FILENAME = "description.yml";

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
	private MysqlService mysqlService;

	private Session _session;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private BorgService borgService;

	@Autowired
	private SchedulerService schedulerTaskFacade;

	@Autowired
	private ReusableCronService reusableCronService;

	@Autowired
	private MailAddressService mailAddressService;

	@Autowired
	private MySqlInstaller mySqlInstaller;

	@Autowired
	private LocaledMessageService localedMessageService;

	@Autowired
	private BoxService boxService;

	@PostConstruct
	public void post() {

	}

	private Session getSession() {
		if (_session == null || !_session.isConnected()) {
			FacadeResult<Session> frSession = sshSessionFactory.getConnectedSession(appState.currentBox().get());
			if (frSession.isExpected()) {
				_session = frSession.getResult();
			} else {
				if (frSession.getException() != null) {
					if (frSession.getException().getMessage().contains("Auth fail")) {
						throw new ServerConnectionException("jsch.connect.authfailed", "認證失敗，請檢查sshKey的配置。");
					} else if (frSession.getException().getMessage().contains("Connection timed out")) {
						throw new ServerConnectionException("jsch.connect.failed", "認證失敗，請檢查sshKey的配置。");
					}
				}
				throw new ServerConnectionException("jsch.connect.failed", "无法链接服务器，请查看日志确定故障原因。");
			}
		}
		return _session;
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
		return FacadeResult.doneExpectedResult(appState.currentBox().get(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "和修改服务器描述")
	public FacadeResult<?> serverUpdate(
			@ShellOption(help = "用户名") String username,
			@ShellOption(help = "密码") String password,
			@ShellOption(help = "服务器角色") String boxRole,
			@ShellOption(help = "SSH端口") int port) throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		box.setUsername(username);
		box.setPassword(password);
		box.setRole(boxRole);
		box.setPort(port);
		boxService.writeDescription(box);
		return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
	}

	@ShellMethod(value = "显示配置相关信息。")
	public List<String> SystemInfo() throws IOException {
		return Arrays.asList(formatKeyVal("数据文件路径", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("database url", environment.getProperty("spring.datasource.url")),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
	}

	@ShellMethod(value = "加载示例服务器。")
	public String loadDemoServer() throws IOException {
		InputStream is = ClassLoader.class.getResourceAsStream("/demobox.yml");
		Box box = YamlInstance.INSTANCE.yaml.loadAs(is, Box.class);
		if (appState.addServer(box)) {
			appState.setCurrentIndexAndFireEvent(appState.getServers().size() - 1);
		}

		return String.format("Demo server %s loaded.", box.getHost());
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
			@ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。", defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME) String logBinValue)
			throws JSchException, IOException {
		sureBoxSelected();
		return mysqlService.mysqlEnableLogbin(getSession(), appState.currentBox().get(), logBinValue);
	}

	@ShellMethod(value = "安装borg。")
	public FacadeResult<?> borgInstall() {
		sureBorgConfigurated();
		return borgService.install(getSession());
	}

	@ShellMethod(value = "查看Borg的描述")
	public FacadeResult<?> borgDescription() throws JSchException, IOException {
		sureBorgConfigurated();
		return FacadeResult.doneExpectedResult(appState.currentBox().get().getBorgBackup(), CommonActionResult.DONE);
	}
	
	@ShellMethod(value = "創建Borg的描述")
	public FacadeResult<?> borgDescriptionCreate()
			throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		BorgBackupDescription bbd = box.getBorgBackup();
		if (bbd != null) {
			FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
		}
		bbd = new BorgBackupDescription();
		box.setBorgBackup(bbd);
		return borgService.saveBox(box);
	}


	@ShellMethod(value = "更新Borg的描述")
	public FacadeResult<?> borgDescriptionUpdate(
			@ShellOption(help = "borg repo.") String repo,
			@ShellOption(help = "borg archive format.") String archiveFormat,
			@ShellOption(help = "borg archive prefix.") String archiveNamePrefix,
			@ShellOption(help = "borg archive cron expression.") String archiveCron,
			@ShellOption(help = "borg prune cron expression.") String pruneCron)
			throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		return borgService.updateBorgDescription(box, repo, archiveFormat, archiveNamePrefix, archiveCron,
				pruneCron);
	}

	@ShellMethod(value = "列出Borg备份包含的目录")
	public List<String> borgIncludesList() throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi == null) {
			return Arrays.asList(localedMessageService.getMessage("command.borg.norepo"));
		}
		return bbdi.getIncludes();
	}

	@ShellMethod(value = "列出Borg备份排除的目录")
	public List<String> borgExcludesList() throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi == null) {
			return Arrays.asList(localedMessageService.getMessage("command.borg.norepo"));
		}
		return bbdi.getExcludes();
	}

	@ShellMethod(value = "添加Borg备份或排除的目录")
	public List<String> borgIncludesExcludesAdd(
			@ShellOption(help = "Absolute directory to add to 'includes'.", defaultValue = "") String include,
			@ShellOption(help = "Absolute directory to add to 'excludes'.", defaultValue = "") String exclude)
			throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi == null) {
			return Arrays.asList(localedMessageService.getMessage("command.borg.norepo"));
		}
		borgService.updateBorgDescription(getSession(), box, include, exclude, true);
		return bbdi.getExcludes();
	}

	@ShellMethod(value = "删除Borg备份或排除的目录")
	public List<String> borgIncludesExcludesRemove(
			@ShellOption(help = "Absolute directory to remove from 'includes'.", defaultValue = "") String include,
			@ShellOption(help = "Absolute directory to remove from 'excludes'.", defaultValue = "") String exclude)
			throws JSchException, IOException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi == null) {
			return Arrays.asList(localedMessageService.getMessage("command.borg.norepo"));
		}
		borgService.updateBorgDescription(getSession(), box, include, exclude, false);
		return bbdi.getExcludes();
	}

	@ShellMethod(value = "安装MYSQL到目标机器")
	public String mysqlInstall(
			@ShellOption(help = "两位数的版本号比如，55,56,57,80。") @Pattern(regexp = "55|56|57|80") String twoDigitVersion,
			@ShellOption(help = "初始root的密码。") @Pattern(regexp = "[^\\s]{5,}") String initPassword) {
		sureBoxSelected();
		Box box = appState.currentBox().get();
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
		Box box = appState.currentBox().get();
		return mySqlInstaller.unInstall(getSession(), box);
	}

	@ShellMethod(value = "初始化borg的repo。")
	public List<String> borgInitRepo() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		return borgService.initRepo(getSession(), box.getBorgBackup().getRepo()).getResult()
				.getAllTrimedNotEmptyLines();
	}

	@ShellMethod(value = "创建一次borg备份")
	public FacadeResult<?> borgCreateArchive() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		return borgService.archive(getSession(), box, box.getBorgBackup().getArchiveNamePrefix());
	}

	@ShellMethod(value = "下载borg的仓库。")
	public void borgDownloadRepo() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		borgService.downloadRepo(getSession(), box);
	}

	@ShellMethod(value = "列出borg创建的卷")
	public List<String> borgListArchives() {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		return borgService.listArchives(getSession(), box).getResult().getArchives();
	}

	@ShellMethod(value = "修剪borg创建的卷")
	public String borgPruneArchives() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		BorgPruneResult bpr = borgService.pruneRepo(getSession(), box).getResult();
		return String.format("action: %s, pruned: %s, keeped: %s", bpr.isSuccess(), bpr.prunedArchiveNumbers(),
				bpr.keepedArchiveNumbers());
	}

	@ShellMethod(value = "列出borg仓库的文件，这些文件的意义由borg来解释。")
	public List<String> borgListRepoFiles() throws RunRemoteCommandException {
		sureBorgConfigurated();
		Box box = appState.currentBox().get();
		return borgService.listRepoFiles(getSession(), box).getResult().getAllTrimedNotEmptyLines();
	}

	private void sureBoxSelected() {
		if (!appState.currentBox().isPresent()) {
			throw new NoServerSelectedException(BackupCommandMsgKeys.SERVER_MISSING,
					"选择一个目标服务器先。 server-list, server-select.");
		}
	}

	private void sureBorgConfigurated() {
		sureBoxSelected();
		if (appState.currentBox().get().getBorgBackup() == null) {
			throw new ShowToUserException("borg.unconfigurated", "");
		}
	}

	private void sureMysqlConfigurated() {
		sureBoxSelected();
		if (appState.currentBox().get().getMysqlInstance() == null) {
			throw new ShowToUserException("mysql.unconfigurated", "");
		}
	}

	@ShellMethod(value = "执行Mysqldump命令")
	public FacadeResult<?> mysqlDump() throws JSchException, IOException {
		sureBoxSelected();
		return mysqlService.mysqlDump(getSession(), appState.currentBox().get());
	}

	@ShellMethod(value = "添加或更改Mysql的描述")
	public FacadeResult<?> mysqlDescriptionUpdate(
			@ShellOption(help = "mysql username.") String username,
			@ShellOption(help = "mysql password.") String password,
			@ShellOption(help = "mysql port.") int port,
			@ShellOption(help = "mysql flush log cron expresion.") String flushLogCron)
			throws JSchException, IOException {
		sureMysqlConfigurated();
		Box box = appState.currentBox().get();
		return mysqlService.updateMysqlDescription(box, username, password, port, flushLogCron);
	}
	
	@ShellMethod(value = "创建Mysql的描述")
	public FacadeResult<?> mysqlDescriptionCreate(
			@ShellOption(help = "mysql username.") String username,
			@ShellOption(help = "mysql password.") String password)
			throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		MysqlInstance mi = box.getMysqlInstance();
		if (mi != null) {
			return FacadeResult.doneExpectedResult(mi, CommonActionResult.DONE);
		}
		mi = new MysqlInstance();
		mi.setUsername(username);
		mi.setPassword(password);
		box.setMysqlInstance(mi);
		return mysqlService.updateMysqlDescription(box);
	}

	@ShellMethod(value = "查看Mysql的描述")
	public FacadeResult<?> mysqlDescription() throws JSchException, IOException {
		sureMysqlConfigurated();
		return FacadeResult.doneExpectedResult(appState.currentBox().get().getMysqlInstance(), CommonActionResult.DONE);
	}

	@ShellMethod(value = "手动flush Mysql的日志")
	public FacadeResult<?> MysqlFlushLog() {
		sureBoxSelected();
		return mysqlService.mysqlFlushLogs(getSession(), appState.currentBox().get());
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
	public List<String> cronExpressionList() {
		return reusableCronService.findAll().stream().map(Objects::toString).collect(Collectors.toList());

	}

	@ShellMethod(value = "添加通知邮件地址。")
	public String emailAdd(@ShellOption(help = "email地址") String email,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		mailAddressService.save(new MailAddress(email, description));
		return "Added.";
	}

	@ShellMethod(value = "列出通知邮件地址。")
	public List<String> emailList() {
		return mailAddressService.findAll().stream().map(Objects::toString).collect(Collectors.toList());
	}

	/**
	 * 再次执行Mysqldump命令之前必须确保mysql flushlogs任务已经结束。
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	@ShellMethod(value = "再次执行Mysqldump命令")
	public FacadeResult<?> mysqlRedump(@Pattern(regexp = "I know what i am doing\\.") String iknow)
			throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return mysqlService.mysqlDump(getSession(), box, true);
	}



	private String getPromptString() {
		switch (appState.getStep()) {
		case WAITING_SELECT:
			return "Please choose an instance by preceding number: ";
		default:
			if (appState.currentBox().isPresent()) {
				return appState.currentBox().get().getHost() + "> ";
			} else {
				return "serverbackup> ";
			}
		}
	}

	// @ShellMethod(value = "Quartz 触发器")
	// public List<String> quartzListTriggers(
	// @Pattern(regexp = "^.+\\..+$") @ShellOption(help = "任务标识，Group.name的形式，
	// 不知道的话可以先执行quartz-list-jobs命令。", defaultValue = "a.b") String jobkey)
	// throws SchedulerException {
	// if (jobkey == null || jobkey.trim().isEmpty() || "a.b".equals(jobkey)) {
	// return scheduler.getTriggerGroupNames().stream().flatMap(grn -> {
	// try {
	// return scheduler.getTriggerKeys(groupEquals(grn)).stream();
	// } catch (SchedulerException e) {
	// return Stream.empty();
	// }
	// }).map(jk -> {
	// try {
	// return scheduler.getTrigger(jk);
	// } catch (SchedulerException e) {
	// return null;
	// }
	// }).filter(Objects::nonNull).map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
	// } else {
	// String[] ss = jobkey.split("\\.", 2);
	// return scheduler.getTriggersOfJob(jobKey(ss[1],
	// ss[0])).stream().map(ToStringFormat::formatTriggerOutput)
	// .collect(Collectors.toList());
	// }
	// }

	// @ShellMethod(value = "Quartz 取消出发器")
	// public void quartzUnscheduleJob(String triggerName, String triggerGroup)
	// throws SchedulerException {
	// scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));
	// }

	// @ShellMethod(value = "Quartz 设置出发器")
	// public void quartzScheduleJob(String triggerName, String triggerGroup, String
	// jobName, String jobGroup)
	// throws SchedulerException {
	// Trigger trigger = newTrigger().withIdentity(triggerName,
	// triggerGroup).startNow()
	// .forJob(jobKey(jobName, jobGroup)).build();
	//
	// // Schedule the trigger
	// scheduler.scheduleJob(trigger);
	// }

	// @ShellMethod(value = "Quartz 设置出发器")
	// public void quartzRescheduleJob(String triggerName, String triggerGroup)
	// throws SchedulerException {
	// Trigger trigger = newTrigger().withIdentity(triggerName,
	// triggerGroup).startNow().build();
	// scheduler.rescheduleJob(triggerKey(triggerName, triggerGroup), trigger);
	// }

	@ShellMethod(value = "重新设置出发器")
	public void schedulerRescheduleJob(String triggerKey, String cronExpression)
			throws SchedulerException, ParseException {
		sureBoxSelected();
		schedulerTaskFacade.schedulerRescheduleJob(triggerKey, cronExpression);
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
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.filter(jk -> jk.getName().equals(appState.currentBox().get().getHost())).map(jk -> jk.toString())
				.collect(Collectors.toList());
	}

	@ShellMethod(value = "列出当前主机的计划任务触发器")
	public List<String> schedulerTriggerList() throws SchedulerException {
		sureBoxSelected();
		return schedulerTaskFacade.getBoxTriggers(appState.currentBox().get()).stream()
				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
	}
	
	@ShellMethod(value = "删除计划任务触发器")
	public FacadeResult<?> schedulerTriggerDelete(@ShellOption(help = "Trigger的名称。") String triggerKey) {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return schedulerTaskFacade.delteBoxTriggers(box, triggerKey);
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
