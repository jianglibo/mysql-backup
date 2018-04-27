package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
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
import com.go2wheel.mysqlbackup.event.ServerChangeEvent;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
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
	
	private Session currentSession;
	
	@PostConstruct
	public void post() {
		
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
	public String createServer(@ShellOption(help = "服务器主机名或者IP") String host, @ShellOption(help = "SSH端口", defaultValue = "22") int sshPort) throws IOException {
		if(Files.exists(appSettings.getDataRoot().resolve(host))) {
			return "该主机已经存在！";
		}
		Box box = new Box();
		box.setHost(host);
		box.setPort(sshPort);
		box.setMysqlInstance(new MysqlInstance());
		mysqlUtil.writeDescription(box);
		return String.format("配置文件：%s已创建在%s目录下 ，请编辑修改参数，请填写你知道的参数即可。", DESCRIPTION_FILENAME, appSettings.getDataRoot().resolve(host));
	}
	
	@ShellMethod(value = "显示配置相关信息。")
	public List<String> SystemInfo() throws IOException {
		return Arrays.asList(
				formatKeyVal("数据文件路径", appSettings.getDataRoot().toAbsolutePath().toString()),
				formatKeyVal("Spring active profile", String.join(",", environment.getActiveProfiles()))
				);
	}
	
	@ShellMethod(value = "加载示例服务器。")
	public void loadDemoServer() throws IOException {
		InputStream is =ClassLoader.class.getResourceAsStream("/demobox.yml");
		Box box =  YamlInstance.INSTANCE.getYaml().loadAs(is, Box.class);
		appState.getServers().add(box);
		appState.setCurrentIndexAndFireEvent(appState.getServers().size() - 1);
	}
	
	private String formatKeyVal(String k, String v) {
		return String.format("%s: %s", k, v);
	}
	
	@EventListener
	public void whenServerChanged(ServerChangeEvent sce) {
		if (currentSession != null) {
			try {
				currentSession.disconnect();
			} catch (Exception e) {
			}
		}
		currentSession = sshSessionFactory.getConnectedSession(appState.currentBox().get()).get();
	}
	
	
	/**
	 * 1. check if already initialized.
	 * 2. get my.cnf content
	 * 3. check if 
	 * @return
	 * @throws IOException 
	 * @throws JSchException 
	 */
	@ShellMethod(value = "为备份MYSQL作准备。")
	public String mysqlPrepareBackup(@ShellOption(help = "重新初始化。") boolean force) throws JSchException, IOException {
		if (!appState.currentBox().isPresent()) {
			return "请先执行list-server和select-server确定使用哪台服务器。";
		}
		LogBinSetting lbs = mysqlUtil.getLogbinState(currentSession, appState.currentBox().get());
		// log_bin doesn't enabled.
		if (lbs.isEnabled()) {
			// enable log_bin.
		} else {
			
		}
		
		return force + "";
	}
	
	
//	@ShellMethod(value = "Create a mysql instance.")
//	public ExecuteResult<MysqlInstance> createInstance(@NotNull String host,
//			@ShellOption(defaultValue = "22") @NotNull @Pattern(regexp = "[1-9][0-9]*") int sshPort,
//			@ShellOption(defaultValue = "3306") @NotNull @Pattern(regexp = "[1-9][0-9]*") int mysqlPort,
//			@ShellOption(defaultValue = "") String sshKeyFile,
//			@ShellOption(defaultValue = "root") @NotNull  String username,
//			@ShellOption(defaultValue = "") String password) {
//		
//		if (password.isEmpty() && sshKeyFile.isEmpty()) {
//			return ExecuteResult.failedResult("Either sshKeyFile or password is required!");
//		}
//		
//		Path ph = instancesBase.resolve(host);
//		if (Files.exists(ph)) {
//			return ExecuteResult.failedResult(String.format("Host: '%s' already exists.", host));
//		}
//		
//		MysqlInstance mi = new MysqlInstance();
//		mi.setHost(host);
//		mi.setUsername(username);
//		mi.setMysqlPort(mysqlPort == 0 ? 3306 : mysqlPort);
//		mi.setSshPort(sshPort == 0 ? 22 : sshPort);
//		if (password.isEmpty()) {
//			if (!Files.exists(Paths.get(sshKeyFile))) {
//				return ExecuteResult.failedResult(String.format("sshKeyFile: '%s' doesn't exists.", sshKeyFile));
//			} else {
//				mi.setSshKeyFile(Paths.get(sshKeyFile).toAbsolutePath().normalize().toString());
//			}
//		} else {
//			mi.setPassword(password);
//		}
//		return writeInstance(mi);
//	}
	
//	private ExecuteResult<MysqlInstance> writeInstance(MysqlInstance mi) {
//		Path mp = instancesBase.resolve(mi.getHost());
//		if (!Files.exists(mp)) {
//			try {
//				Files.createDirectories(mp);
//			} catch (IOException e) {
//				return ExecuteResult.failedResult(String.format("Create directory: '%s' failed.", mp.toString()));
//			}
//		}
//		Path df = mp.resolve(DESCRIPTION_FILENAME);
//		
//		String s = YamlInstance.INSTANCE.getYaml().dumpAsMap(mi);
//		try (BufferedWriter bw = Files.newBufferedWriter(df)) {
//			bw.write(s);
//			bw.flush();
//			bw.close();
//		} catch (IOException e) {
//			return ExecuteResult.failedResult(String.format("Write Yml file : '%s' failed.", df.toString()));
//		}
//		return new ExecuteResult<>(mi).setMessage(String.format("Mysql on host: %s created.", mi.getHost())); 
//	}

//	protected ListBoxResult listInstanceInternal() throws IOException {
//		allInstancePaths = Files.list(appSettings.getDataRoot()).collect(Collectors.toList());
//		return new ListBoxResult(allInstancePaths);
//	}
	
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
	
	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
