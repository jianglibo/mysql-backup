package com.go2wheel.mysqlbackup.commands;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Pattern;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
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
import com.go2wheel.mysqlbackup.borg.BorgTaskFacade;
import com.go2wheel.mysqlbackup.event.ServerChangeEvent;
import com.go2wheel.mysqlbackup.exception.AtomicWriteFileException;
import com.go2wheel.mysqlbackup.exception.CreateDirectoryException;
import com.go2wheel.mysqlbackup.exception.MyCommonException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.InstallationInfo;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@ShellComponent()
public class BackupCommand {

	public static final String DESCRIPTION_FILENAME = "description.yml";

	@Autowired
	private MyAppSettings appSettings;

	@Autowired
	private MysqlUtil mysqlUtil;

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationState appState;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlTaskFacade mysqlTaskFacade;

	private Session _session;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private BorgTaskFacade borgInstaller;

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
	public ApplicationState listServer() throws IOException {
		return appState;
	}

	@ShellMethod(value = "Pickup a server to work on.")
	public ApplicationState selectServer() throws IOException {
		this.appState.setStep(CommandStepState.WAITING_SELECT);
		return appState;
	}

	@ShellMethod(value = "新建一个服务器.")
	public String createServer(@ShellOption(help = "服务器主机名或者IP") String host,
			@ShellOption(help = "SSH端口", defaultValue = "22") int sshPort) throws IOException {
		if (Files.exists(appSettings.getDataRoot().resolve(host))) {
			return "该主机已经存在！";
		}
		Box box = new Box();
		box.setHost(host);
		box.setPort(sshPort);
		box.setMysqlInstance(new MysqlInstance());
		try {
			mysqlUtil.writeDescription(box);
		} catch (CreateDirectoryException | AtomicWriteFileException e) {
			return e.getMessage();
		}
		return String.format("配置文件：%s已创建在%s目录下 ，请编辑修改参数，请填写你知道的参数即可。", DESCRIPTION_FILENAME,
				appSettings.getDataRoot().resolve(host));
	}

	@ShellMethod(value = "显示配置相关信息。")
	public List<String> SystemInfo() throws IOException {
		return Arrays.asList(formatKeyVal("数据文件路径", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles())));
	}

	@ShellMethod(value = "加载示例服务器。")
	public void loadDemoServer() throws IOException {
		InputStream is = ClassLoader.class.getResourceAsStream("/demobox.yml");
		Box box = YamlInstance.INSTANCE.getYaml().loadAs(is, Box.class);
		appState.getServers().add(box);
		appState.setCurrentIndexAndFireEvent(appState.getServers().size() - 1);
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
		if (!appState.currentBox().isPresent()) {
			return "请先执行list-server和select-server确定使用哪台服务器。";
		}
		try {
			return mysqlTaskFacade.mysqlEnableLogbin(getSession(), appState.currentBox().get(), logBinValue);
		} catch (CreateDirectoryException | AtomicWriteFileException | RunRemoteCommandException e) {
			return e.getMessage();
		}
	}
	
	@ShellMethod(value = "安装borg。")
	public String borgInstall() {
		try {
			InstallationInfo ii = borgInstaller.install(getSession());
			if (ii.isInstalled()) {
				return "Success";
			} else {
				return ii.getFailReason();
			}
			
		} catch (RunRemoteCommandException e) {
			return e.getMessage();
		}
	}

	private void sureBoxSelected() {
		if (!appState.currentBox().isPresent()) {
			throw new MyCommonException("no selected server", "请先执行list-server和select-server确定使用哪台服务器。");
		}

	}

	@ShellMethod(value = "执行Mysqldump命令")
	public MysqlDumpResult mysqlDump() throws JSchException, IOException {
		sureBoxSelected();
		return mysqlTaskFacade.mysqlDump(getSession(), appState.currentBox().get());
	}

	/**
	 * 再次执行Mysqldump命令之前必须确保mysql flushlogs任务已经结束。
	 * 
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	@ShellMethod(value = "再次执行Mysqldump命令")
	public MysqlDumpResult mysqlRedump(
			@Pattern(regexp = "I know what i am doing\\.") @ShellOption(help = "请输入参数值'I know what i am doing.'") String iknow) throws JSchException, IOException {
		sureBoxSelected();
		Box box = appState.currentBox().get();
		return mysqlTaskFacade.mysqlDump(getSession(), box, true);
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

	@ShellMethod(value = "Quartz 触发器")
	public List<String> quartzListTriggers(
			@Pattern(regexp = "^.+\\..+$") @ShellOption(help = "任务标识，Group.name的形式， 不知道的话可以先执行quartz-list-jobs命令。", defaultValue = "a.b") String jobkey)
			throws SchedulerException {
		if (jobkey == null || jobkey.trim().isEmpty() || "a.b".equals(jobkey)) {
			return scheduler.getTriggerGroupNames().stream().flatMap(grn -> {
				try {
					return scheduler.getTriggerKeys(groupEquals(grn)).stream();
				} catch (SchedulerException e) {
					return Stream.of();
				}
			}).map(jk -> {
				try {
					return scheduler.getTrigger(jk);
				} catch (SchedulerException e) {
					return null;
				}
			}).filter(Objects::nonNull).map(BackupCommand::formatTriggerOutput).collect(Collectors.toList());
		} else {
			String[] ss = jobkey.split("\\.", 2);
			return scheduler.getTriggersOfJob(jobKey(ss[1], ss[0])).stream().map(BackupCommand::formatTriggerOutput)
					.collect(Collectors.toList());
		}
	}

	@ShellMethod(value = "Quartz 取消出发器")
	public void quartzUnscheduleJob(String triggerName, String triggerGroup) throws SchedulerException {
		scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));
	}

	@ShellMethod(value = "Quartz 设置出发器")
	public void quartzScheduleJob(String triggerName, String triggerGroup, String jobName, String jobGroup)
			throws SchedulerException {
		Trigger trigger = newTrigger().withIdentity(triggerName, triggerGroup).startNow()
				.forJob(jobKey(jobName, jobGroup)).build();

		// Schedule the trigger
		scheduler.scheduleJob(trigger);
	}

	@ShellMethod(value = "Quartz 设置出发器")
	public void quartzRescheduleJob(String triggerName, String triggerGroup) throws SchedulerException {
		Trigger trigger = newTrigger().withIdentity(triggerName, triggerGroup).startNow().build();
		scheduler.rescheduleJob(triggerKey(triggerName, triggerGroup), trigger);
	}

	@ShellMethod(value = "Quartz 删除任务")
	public void quartzDeleteJob(String triggerName, String triggerGroup) throws SchedulerException {
		scheduler.deleteJob(jobKey(triggerName, triggerGroup));
	}

	private static String formatTriggerOutput(Trigger t) {
		String tostr = t.toString();
		if (CronTrigger.class.isAssignableFrom(t.getClass())) {
			tostr = tostr + ", cron: " + ((CronTrigger) t).getCronExpression();
		}
		return tostr;
	}

	@ShellMethod(value = "Quartz 显示任务")
	public List<String> quartzListJobs(@ShellOption(help = "任务组的名称", defaultValue = "") String groupName)
			throws SchedulerException {
		if (groupName == null || groupName.trim().isEmpty()) {
			return scheduler.getJobGroupNames().stream().flatMap(grn -> {
				try {
					return scheduler.getJobKeys(groupEquals(grn)).stream();
				} catch (SchedulerException e) {
					return Stream.of();
				}
			}).map(jk -> jk.toString()).collect(Collectors.toList());
		} else {
			return scheduler.getJobKeys(groupEquals(groupName)).stream().map(jk -> jk.toString())
					.collect(Collectors.toList());
		}

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

	}

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(),
				AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
