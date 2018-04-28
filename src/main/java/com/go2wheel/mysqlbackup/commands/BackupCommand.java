package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

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
	
	@Autowired
	private MysqlTaskFacade mysqlTaskFacade;
	
	private Session _session;
	
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
		if (_session != null) {
			try {
				_session.disconnect();
			} catch (Exception e) {
			}
		}
		_session = sshSessionFactory.getConnectedSession(appState.currentBox().get()).get();
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
	public String mysqlEnableLogbin(@ShellOption(help = "Mysql log_bin的值，如果mysql已经启用logbin，不会尝试去更改它。" , defaultValue = MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME)String logBinValue) throws JSchException, IOException {
		if (!appState.currentBox().isPresent()) {
			return "请先执行list-server和select-server确定使用哪台服务器。";
		}
		return mysqlTaskFacade.mysqlEnableLogbin(getSession(), appState.currentBox().get(), logBinValue);
	}
	
	@ShellMethod(value = "执行Mysqldump命令")
	public String mysqlDump() throws JSchException, IOException {
		if (!appState.currentBox().isPresent()) {
			return "请先执行list-server和select-server确定使用哪台服务器。";
		}
		return mysqlTaskFacade.mysqlDump(getSession(), appState.currentBox().get());
	}
	
	@ShellMethod(value = "显示当前选定服务器的描述文件。")
	public String serverDescription() throws JSchException, IOException {
		if (!appState.currentBox().isPresent()) {
			return "请先执行list-server和select-server确定使用哪台服务器。";
		}
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
	
	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
