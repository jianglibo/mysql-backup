package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
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
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.event.ServerChangeEvent;
import com.go2wheel.mysqlbackup.exception.NoServerSelectedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
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
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
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
	private MysqlService mysqlTaskFacade;

	private Session _session;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private BorgService borgTaskFacade;

	@Autowired
	private SchedulerService schedulerTaskFacade;
	
	@Autowired
	private ReusableCronService reusableCronService;
	
	@Autowired
	private MailAddressService mailAddressService;
	
	@Autowired
	private MySqlInstaller mySqlInstaller;
	
	@PostConstruct
	public void post() {

	}

	private Session getSession() {
		if (_session == null || !_session.isConnected()) {
			_session = sshSessionFactory.getConnectedSession(appState.currentBox().get()).get();
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
	public String serverCreate(@ShellOption(help = "服务器主机名或者IP") String host,
			@ShellOption(help = "SSH端口", defaultValue = "22") int sshPort) throws IOException {
		return actionResultToString(mysqlTaskFacade.serverCreate(host, sshPort));
	}

	@ShellMethod(value = "显示配置相关信息。")
	public List<String> SystemInfo() throws IOException {
		return Arrays.asList(formatKeyVal("数据文件路径", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
	}

	@ShellMethod(value = "加载示例服务器。")
	public String loadDemoServer() throws IOException {
		InputStream is = ClassLoader.class.getResourceAsStream("/demobox.yml");
		Box box = YamlInstance.INSTANCE.getYaml().loadAs(is, Box.class);
		if (appState.addServer(box)) {
			appState.setCurrentIndexAndFireEvent(appState.getServers().size() - 1);
		}

		return String.format("Demo server %s loaded.", box.getHost());
	}

	private String formatKeyVal(String k, String v) {
		return String.format("%s: %s", k, v);
	}

	@EventListener
	public void whenServerChanged(ServerChangeEvent sce) {
		if (_session != null) {
			try {
				_session.disconnect();
			} catch (Exception e) {
			}
		}
		_session = sshSessionFactory.getConnectedSession(appState.currentBox().get()).get();
	}

	/**
	 * 1. check if already initialized. 2. get my.cnf content 3. check if
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSchException
	 */
	@ShellMethod(value = "为备份MYSQL作准备。")
	public String mysqlEnableLogbin(
			@ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。", defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME) String logBinValue)
			throws JSchException, IOException {
		sureBoxSelected();
		return actionResultToString(mysqlTaskFacade.mysqlEnableLogbin(getSession(), appState.currentBox().get(), logBinValue));
	}

	@ShellMethod(value = "安装borg。")
	public String borgInstall() {
		return actionResultToString(borgTaskFacade.install(getSession()));
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

	

	@ShellMethod(value = "初始化borg的repo。")
	public List<String> borgInitRepo() throws RunRemoteCommandException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return borgTaskFacade.initRepo(getSession(),box.getBorgBackup().getRepo()).getResult()
				.getAllTrimedNotEmptyLines();
	}

	@ShellMethod(value = "创建一次borg备份")
	public String borgCreateArchive() throws RunRemoteCommandException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		borgTaskFacade.archive(getSession(), box, box.getBorgBackup().getArchiveNamePrefix());
		return "Success";
	}
	
	@ShellMethod(value = "下载borg的仓库。")
	public void borgDownloadRepo() throws RunRemoteCommandException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		borgTaskFacade.downloadRepo(getSession(), box);
	}

	@ShellMethod(value = "列出borg创建的卷")
	public List<String> borgListArchives() {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return borgTaskFacade.listArchives(getSession(), box).getResult().getArchives();
	}
	
	@ShellMethod(value = "修剪borg创建的卷")
	public String borgPruneArchives() throws RunRemoteCommandException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		BorgPruneResult bpr = borgTaskFacade.pruneRepo(getSession(), box).getResult(); 
		return String.format("action: %s, pruned: %s, keeped: %s", bpr.isSuccess(), bpr.prunedArchiveNumbers(), bpr.keepedArchiveNumbers());
	}

	@ShellMethod(value = "列出borg仓库的文件，这些文件的意义由borg来解释。")
	public List<String> borgListRepoFiles() throws RunRemoteCommandException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return borgTaskFacade.listRepoFiles(getSession(), box).getResult().getAllTrimedNotEmptyLines();
	}

	private void sureBoxSelected() {
		if (!appState.currentBox().isPresent()) {
			throw new NoServerSelectedException("选择一个目标服务器先。 server-list, server-select.");
		}
	}

	@ShellMethod(value = "执行Mysqldump命令")
	public String mysqlDump() throws JSchException, IOException {
		sureBoxSelected();
		return actionResultToString(mysqlTaskFacade.mysqlDump(getSession(), appState.currentBox().get()));
	}

	@ShellMethod(value = "手动flush Mysql的日志")
	public String MysqlFlushLog() {
		sureBoxSelected();
		return actionResultToString(mysqlTaskFacade.mysqlFlushLogs(getSession(), appState.currentBox().get()));
	}
	
	private String actionResultToString(FacadeResult<?> fr) {
		return "yes";
	}
	
	
	@ShellMethod(value = "添加常用的CRON表达式")
	public String cronExpressionAdd(
			@ShellOption(help = "cron表达式") String expression,
			@ShellOption(help = "描述", defaultValue = "") String description) {
		reusableCronService.save(new ReusableCron(expression, description));
		return "Added.";
	}

	@ShellMethod(value = "列出常用的CRON表达式")
	public List<String> cronExpressionList() {
		return reusableCronService.findAll().stream().map(Objects::toString).collect(Collectors.toList());
		
	}

	@ShellMethod(value = "添加通知邮件地址。")
	public String emailAdd(
			@ShellOption(help = "email地址") String email,
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
	public String mysqlRedump(
			@Pattern(regexp = "I know what i am doing\\.") @ShellOption(help = "@see 'http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html'") String iknow)
			throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return actionResultToString(mysqlTaskFacade.mysqlDump(getSession(), box, true));
	}

	@ShellMethod(value = "显示当前选定服务器的描述文件。")
	public String serverDescription() throws JSchException, IOException {
		sureBoxSelected();
		return YamlInstance.INSTANCE.getYaml().dumpAsMap(appState.currentBox().get());
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

	// @ShellMethod(value = "Quartz 删除任务")
	// public void quartzDeleteJob(String triggerName, String triggerGroup) throws
	// SchedulerException {
	// scheduler.deleteJob(jobKey(triggerName, triggerGroup));
	// }

	@ShellMethod(value = "列出当前主机的计划任务")
	public List<String> schedulerListJob() throws SchedulerException {
		sureBoxSelected();
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup()).stream()
				.filter(jk -> jk.getName().equals(appState.currentBox().get().getHost())).map(jk -> jk.toString())
				.collect(Collectors.toList());
	}

	@ShellMethod(value = "列出当前主机的计划任务触发器")
	public List<String> schedulerListTrigger() throws SchedulerException {
		sureBoxSelected();
		return schedulerTaskFacade.getBoxTriggers(appState.currentBox().get()).stream()
				.map(ToStringFormat::formatTriggerOutput).collect(Collectors.toList());
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

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
