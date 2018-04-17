package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.value.ExecuteResult;

@ShellComponent()
public class BackupCommand {
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, ON_INSTANCE
	}
	
	private List<Path> allInstancePaths;
	
	private Path workingPath;
	
	private CommandStepState state = CommandStepState.INIT_START;
	
	@ShellMethod(value = "List all managed mysql instances.")
	public ExecuteResult<List<String>> listInstance() throws IOException {
		return listInstance(PathUtil.getJarLocation().get().resolve("mysqls"));
	}
	
	@ShellMethod(value = "Pickup an instance as working instance.")
	public ExecuteResult<List<String>> selectInstance() throws IOException {
		this.state = CommandStepState.WAITING_SELECT;
		return listInstance(PathUtil.getJarLocation().get().resolve("mysqls"));
	}
	
	protected ExecuteResult<List<String>> listInstance(Path instancesBasePath) throws IOException {
		allInstancePaths = Files.list(instancesBasePath).collect(Collectors.toList());
		List<String> ss = new ArrayList<>();
		for(int i = 0; i < allInstancePaths.size(); i++) {
			ss.add(String.format("%s  %s", i, allInstancePaths.get(i).getFileName()));
		}
		return new ExecuteResult<>(ss);
	}
	
	private String getPromptString() {
		switch (state) {
		case WAITING_SELECT:
			return "please entry the number before the hostnames:>";
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
