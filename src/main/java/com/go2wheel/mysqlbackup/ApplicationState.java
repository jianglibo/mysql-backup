package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.event.ServerChangeEvent;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Component
public class ApplicationState {
	
	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	@Autowired
	private MyAppSettings appSettings;
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	private List<Box> servers = new ArrayList<>();
	
	private int currentIndex;
	
	private CommandStepState step = CommandStepState.INIT_START;
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}
	
	@PostConstruct
	public void post() throws IOException {
		servers = Files.list(appSettings.getDataRoot()).filter(Files::isDirectory).map(p -> p.resolve(BackupCommand.DESCRIPTION_FILENAME)).map(p -> {
			try {
				return YamlInstance.INSTANCE.getYaml().loadAs(Files.newInputStream(p), Box.class);
			} catch (IOException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	public List<Box> getServers() {
		return servers;
	}

	public void setServers(List<Box> servers) {
		this.servers = servers;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public void setCurrentIndexAndFireEvent(int newCurrentIndex) {
		if (newCurrentIndex != currentIndex || step == CommandStepState.INIT_START) {
			this.currentIndex = newCurrentIndex;
			ServerChangeEvent sce = new ServerChangeEvent(this);
			applicationEventPublisher.publishEvent(sce);
		}
	}
	

	public Optional<Box> currentBox() {
		if (servers != null && servers.size() > currentIndex) {
			return Optional.of(servers.get(currentIndex));
		} else {
			return Optional.empty();
		}
	}
	
	public void persistState() throws IOException {
		String s = YamlInstance.INSTANCE.getYaml().dumpAsMap(this);
		Files.write(appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE), s.getBytes());
	}
	
	public void loadState() throws IOException {
		Path p = appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE);
		if (Files.exists(p)) {
			ApplicationState as = YamlInstance.INSTANCE.getYaml().loadAs(Files.newInputStream(p), ApplicationState.class);
			if (as.getCurrentIndex() < getServers().size()) {
				this.currentIndex = as.getCurrentIndex();
			}
		}
	}

	public CommandStepState getStep() {
		return step;
	}

	public void setStep(CommandStepState step) {
		this.step = step;
	}

}
