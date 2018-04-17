package com.go2wheel.mysqlbackup.commands;

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

import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.value.ExecuteResult;
import com.go2wheel.mysqlbackup.value.ListInstanceResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

@ShellComponent()
public class BackupCommand {
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, ON_INSTANCE
	}
	
	private List<Path> allInstancePaths;
	
	private Path workingPath;
	
	private CommandStepState state = CommandStepState.INIT_START;
	
	@ShellMethod(value = "List all managed mysql instances.")
	public ListInstanceResult listInstance() throws IOException {
		return listInstance(PathUtil.getJarLocation().get().resolve("mysqls"));
	}
	
	@ShellMethod(value = "Pickup an instance as working instance.")
	public ListInstanceResult selectInstance() throws IOException {
		this.state = CommandStepState.WAITING_SELECT;
		return listInstance(getInstanceRootDir());
	}
	
	private Path getInstanceRootDir() {
		Path p = PathUtil.getJarLocation().get().resolve("mysqls");
		if (!Files.exists(p)) {
			try {
				Files.createDirectories(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return p;
	}
	
	@ShellMethod(value = "Create a mysql instance.")
	public ExecuteResult<Boolean> createInstance(@NotNull String host,
			@ShellOption(defaultValue = "3306") @NotNull @Pattern(regexp = "[1-9][0-9]*") int port,
			@ShellOption(defaultValue = "") String sshKeyFile,
			@ShellOption(defaultValue = "root") @NotNull  String username,
			@ShellOption(defaultValue = "") String password) {
		
		if (password.isEmpty() && sshKeyFile.isEmpty()) {
			return ExecuteResult.failedResult("Either sshKeyFile or password is required!");
		}
		
		Path ph = getInstanceRootDir().resolve(host);
		if (Files.exists(ph)) {
			return ExecuteResult.failedResult(String.format("Host: '%s' already exists.", host));
		}
		
		MysqlInstance mi = new MysqlInstance();
		mi.setHost(host);
		mi.setUsername(username);
		if (password.isEmpty()) {
			if (!Files.exists(Paths.get(sshKeyFile))) {
				return ExecuteResult.failedResult(String.format("sshKeyFile: '%s' doesn't exists.", sshKeyFile));
			} else {
				mi.setSshKeyFile(Paths.get(sshKeyFile).toAbsolutePath().normalize().toString());
			}
		} else {
			mi.setPassword(password);
		}
		
		
		
		return new ExecuteResult<Boolean>(true);
	}
	
	protected ListInstanceResult listInstance(Path instancesBasePath) throws IOException {
		if (!Files.exists(instancesBasePath)) {
			Files.createDirectories(instancesBasePath);
		}
		allInstancePaths = Files.list(instancesBasePath).collect(Collectors.toList());
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

}
