package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Component
public class ApplicationState {
	
	@Autowired
	private MyAppSettings appSettings;
	
	private List<Box> servers = new ArrayList<>();
	
	private int currentIndex;
	
	private CommandStepState step = CommandStepState.INIT_START;
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}

	
	
	@PostConstruct
	public void post() throws IOException {
		servers = Files.list(appSettings.getDataRoot()).map(p -> {
			try {
				return YamlInstance.INSTANCE.getYaml().loadAs(Files.newInputStream(p), Box.class);
			} catch (IOException e) {
				return null;
			}
		}).filter(b -> !Objects.isNull(b)).collect(Collectors.toList());
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

	public Optional<Box> currentBox() {
		if (servers != null && servers.size() > currentIndex) {
			return Optional.of(servers.get(currentIndex));
		} else {
			return Optional.empty();
		}
		
	}

	public CommandStepState getStep() {
		return step;
	}

	public void setStep(CommandStepState step) {
		this.step = step;
	}

}
