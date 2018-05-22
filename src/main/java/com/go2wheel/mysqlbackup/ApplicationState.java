package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.event.ServerCreateEvent;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.model.BackupFolder;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BackupFolderService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Component
public class ApplicationState implements EnvironmentAware {

	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	public static boolean IS_PROD_MODE = false;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private BackupFolderService bfService;

	@Autowired
	private MyAppSettings appSettings;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private List<Box> servers = new ArrayList<>();
	
	private Environment environment;

	private Locale local = Locale.CHINESE;
	
	private Box currentBox;

	public Box getCurrentBox() {
		return currentBox;
	}

	public void setCurrentBox(Box currentBox) {
		this.currentBox = currentBox;
	}

	private FacadeResult<?> facadeResult;

	private CommandStepState step = CommandStepState.INIT_START;
	
	@Value("${expectit.echo}")
	private boolean expectitEcho;

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
		
		try {
			servers.stream().forEach(box -> {
				Server sv = serverService.findByHost(box.getHost());
				if (sv == null) {
					sv = new Server(box.getHost());
					sv = serverService.save(sv);
				}
				
				final Server svfinal = sv;
				
				if (box.getBorgBackup() != null && box.getBorgBackup().getIncludes() != null) {
					box.getBorgBackup().getIncludes().stream().forEach(fo -> {
						BackupFolder bf = bfService.findByServerHostAndFolder(box.getHost(), fo);
						if (bf == null) {
							bf = new BackupFolder(svfinal.getId(), fo);
							bfService.save(bf);
						}
					});
				}
			});
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		
		ApplicationState.IS_PROD_MODE = !Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> "dev".equals(p));
		loadState();
	}

	public synchronized List<Box> getServers() {
		return servers;
	}

	public void setServers(List<Box> servers) {
		this.servers = servers;
	}

	
	public void fireSwitchEvent() {
		ServerSwitchEvent sce = new ServerSwitchEvent(this);
		applicationEventPublisher.publishEvent(sce);
	}

	public Optional<Box> currentBoxOptional() {
		if (currentBox == null) {
			if (getServers().size() > 0) {
				currentBox = getServers().get(0);
			} else {
				return Optional.empty();
			}
		}
		
		return Optional.of(currentBox);
	}

	public void persistState() {
		String s = YamlInstance.INSTANCE.yaml.dumpAsMap(new PersistState(this));
		try {
			Files.write(appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE), s.getBytes());
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}

	public void loadState() throws IOException {
		try {
			Path p = appSettings.getDataRoot().resolve(APPLICATION_STATE_PERSIST_FILE);
			if (Files.exists(p)) {
				PersistState as = YamlInstance.INSTANCE.yaml.loadAs(Files.newInputStream(p),
						PersistState.class);
				if (StringUtil.hasAnyNonBlankWord(as.getHost())) {
					Box box = getServerByHost(as.getHost());
					if (box != null) {
						setCurrentBox(box);
						fireSwitchEvent();
					}
								
				}

			}
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}
	
	public static class PersistState {
		private String host;
		
		public PersistState() {
		}
		
		public PersistState(ApplicationState appState) {
			if (appState.currentBoxOptional().isPresent()) {
				this.host = appState.currentBoxOptional().get().getHost();
			}
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
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

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}
