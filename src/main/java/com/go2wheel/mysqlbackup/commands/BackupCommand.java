package com.go2wheel.mysqlbackup.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.go2wheel.mysqlbackup.value.ExecuteResult;
import com.go2wheel.mysqlbackup.value.ListInstanceResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@ShellComponent()
public class BackupCommand {
	
	public static final String DESCRIPTION_FILENAME = "description.yml";
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, ON_INSTANCE
	}
	
	private List<Path> allInstancePaths;
	
	private Path workingPath;
	
	private CommandStepState state = CommandStepState.INIT_START;
	
	private Path instancesBase;
	
	@ShellMethod(value = "List all managed mysql instances.")
	public ListInstanceResult listInstance() throws IOException {
		return listInstanceInternal();
	}
	
	@ShellMethod(value = "Pickup an instance as working instance.")
	public ListInstanceResult selectInstance() throws IOException {
		this.state = CommandStepState.WAITING_SELECT;
		return listInstanceInternal();
	}
	
	
	@ShellMethod(value = "Create a mysql instance.")
	public ExecuteResult<MysqlInstance> createInstance(@NotNull String host,
			@ShellOption(defaultValue = "22") @NotNull @Pattern(regexp = "[1-9][0-9]*") int sshPort,
			@ShellOption(defaultValue = "3306") @NotNull @Pattern(regexp = "[1-9][0-9]*") int mysqlPort,
			@ShellOption(defaultValue = "") String sshKeyFile,
			@ShellOption(defaultValue = "root") @NotNull  String username,
			@ShellOption(defaultValue = "") String password) {
		
		if (password.isEmpty() && sshKeyFile.isEmpty()) {
			return ExecuteResult.failedResult("Either sshKeyFile or password is required!");
		}
		
		Path ph = instancesBase.resolve(host);
		if (Files.exists(ph)) {
			return ExecuteResult.failedResult(String.format("Host: '%s' already exists.", host));
		}
		
		MysqlInstance mi = new MysqlInstance();
		mi.setHost(host);
		mi.setUsername(username);
		mi.setMysqlPort(mysqlPort == 0 ? 3306 : mysqlPort);
		mi.setSshPort(sshPort == 0 ? 22 : sshPort);
		if (password.isEmpty()) {
			if (!Files.exists(Paths.get(sshKeyFile))) {
				return ExecuteResult.failedResult(String.format("sshKeyFile: '%s' doesn't exists.", sshKeyFile));
			} else {
				mi.setSshKeyFile(Paths.get(sshKeyFile).toAbsolutePath().normalize().toString());
			}
		} else {
			mi.setPassword(password);
		}
		return writeInstance(mi);
	}
	
	private ExecuteResult<MysqlInstance> writeInstance(MysqlInstance mi) {
		Path mp = instancesBase.resolve(mi.getHost());
		if (!Files.exists(mp)) {
			try {
				Files.createDirectories(mp);
			} catch (IOException e) {
				return ExecuteResult.failedResult(String.format("Create directory: '%s' failed.", mp.toString()));
			}
		}
		Path df = mp.resolve(DESCRIPTION_FILENAME);
		
		String s = YamlInstance.INSTANCE.getYaml().dumpAsMap(mi);
		try (BufferedWriter bw = Files.newBufferedWriter(df)) {
			bw.write(s);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			return ExecuteResult.failedResult(String.format("Write Yml file : '%s' failed.", df.toString()));
		}
		return new ExecuteResult<>(mi).setMessage(String.format("Mysql on host: %s created.", mi.getHost())); 
	}

	protected ListInstanceResult listInstanceInternal() throws IOException {
		allInstancePaths = Files.list(instancesBase).collect(Collectors.toList());
		return new ListInstanceResult(allInstancePaths);
	}
	
	private String getPromptString() {
		switch (state) {
		case WAITING_SELECT:
			return "Please choose an instance by preceding number :>";
		default:
			if (workingPath != null) {
				return workingPath.toString() + ":>";
			} else {
				return "mysqlbackup:>";
			}
		}
	}
	
	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString(getPromptString(), AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}


	public CommandStepState getState() {
		return state;
	}


	public void setState(CommandStepState state) {
		this.state = state;
	}
	
	public Path getInstancesBase() {
		return instancesBase;
	}

	public void setInstancesBase(Path instancesBase) {
		this.instancesBase = instancesBase;
	}
}
