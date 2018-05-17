package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.event.ServerChangeEvent;
import com.go2wheel.mysqlbackup.event.ServerCreateEvent;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Component
public class ApplicationState {

	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MyAppSettings appSettings;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private List<Box> servers = new ArrayList<>();

	private Locale local = Locale.CHINESE;

	private int currentIndex;

	private FacadeResult<?> facadeResult;

	private CommandStepState step = CommandStepState.INIT_START;

	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}

	@PostConstruct
	public void post() throws IOException {
		Path dr = appSettings.getDataRoot();
		try (Stream<Path> vs = Files.list(dr)){
			servers = vs.filter(Files::isDirectory)
					.map(p -> p.resolve(BackupCommand.DESCRIPTION_FILENAME))
					.filter(f -> {
						if (Files.exists(f)) {
							return true;
						} else {
							Path wf = f.getParent().resolve(BackupCommand.DESCRIPTION_FILENAME + ".writing");
							if (Files.exists(wf)) {
								try {
									Files.move(wf, f);
								} catch (IOException e) {
									ExceptionUtil.logErrorException(logger, e);
									return false;
								}
								return true;
							} else {
								return false;
							}
							
						}
					})
					.map(p -> {
						
						try (InputStream is = Files.newInputStream(p)) {
							return YamlInstance.INSTANCE.yaml.loadAs(is, Box.class);
						} catch (IOException e) {
							ExceptionUtil.logErrorException(logger, e);
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toList());
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}

	public synchronized List<Box> getServers() {
		if (servers.isEmpty()) {
			try {
				post();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		String s = YamlInstance.INSTANCE.yaml.dumpAsMap(this);
		Files.write(appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE), s.getBytes());
	}

	public void loadState() throws IOException {
		Path p = appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE);
		if (Files.exists(p)) {
			ApplicationState as = YamlInstance.INSTANCE.yaml.loadAs(Files.newInputStream(p),
					ApplicationState.class);
			if (as.getCurrentIndex() < getServers().size()) {
				this.currentIndex = as.getCurrentIndex();
			}
		}
	}

	public boolean addServer(Box box) {
		if (box == null) {
			return false;
		}
		boolean exists = getServers().stream().anyMatch(b -> b.getHost().equalsIgnoreCase(box.getHost()));
		if (!exists) {
			this.servers.add(box);
			ServerCreateEvent sce = new ServerCreateEvent(this, box);
			applicationEventPublisher.publishEvent(sce);
			return true;
		} else {
			return false;
		}
	}

	public Box getServerByHost(String host) {
		return this.servers.stream().filter(s -> host.equals(s.getHost())).findAny().orElse(null);
	}

	public CommandStepState getStep() {
		return step;
	}

	public void setStep(CommandStepState step) {
		this.step = step;
	}

	public Locale getLocal() {
		return local;
	}

	public void setLocal(Locale local) {
		this.local = local;
	}

	public FacadeResult<?> getFacadeResult() {
		return facadeResult;
	}

	public void setFacadeResult(FacadeResult<?> facadeResult) {
		this.facadeResult = facadeResult;
	}

}
