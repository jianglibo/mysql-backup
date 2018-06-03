package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelCreatedEvent;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.model.KeyValueInDb;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BackupFolderService;
import com.go2wheel.mysqlbackup.service.KeyValueInDbService;
import com.go2wheel.mysqlbackup.service.ServerService;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class ApplicationState implements EnvironmentAware {

	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	public static boolean IS_PROD_MODE = false;
	
	private static final Integer APP_STATE_ID = 0;
	private static final String APP_STATE_NAME = "APP_STATE_NAME";
	private static final String APP_STATE_LAST_SERVER_ID = "LAST_SERVER_ID";
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private BackupFolderService bfService;

	@Autowired
	private MyAppSettings appSettings;
	
	@Autowired
	private KeyValueInDbService keyValueInDbService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private List<Server> servers = new ArrayList<>();
	
	private Environment environment;

	private Locale local = Locale.CHINESE;
	
	private Server currentServer;

	private FacadeResult<?> facadeResult;

	private CommandStepState step = CommandStepState.INIT_START;
	
	@Value("${expectit.echo}")
	private boolean expectitEcho;

	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}

	@PostConstruct
	public void post() throws IOException {
		servers = serverService.findAll().stream().map(s -> serverService.loadFull(s)).collect(Collectors.toList());
		logger.info("load {} servers.", servers.size());
	}
	
	public void fireSwitchEvent() {
		ServerSwitchEvent sce = new ServerSwitchEvent(this);
		applicationEventPublisher.publishEvent(sce);
	}

	public Optional<Server> currentServerOptional() {
		if (currentServer == null) {
			if (getServers().size() > 0) {
				currentServer = getServers().get(0);
			} else {
				return Optional.empty();
			}
		}
		return Optional.of(currentServer);
	}

	public void persistState() {
		if (getCurrentServer() != null) {
			KeyValueInDb kv = keyValueInDbService.findByIdNameKey(APP_STATE_ID, APP_STATE_NAME, APP_STATE_LAST_SERVER_ID);
			if (kv == null) {
				kv = new KeyValueInDb();
				kv.setObjectId(APP_STATE_ID);
				kv.setObjectName(APP_STATE_NAME);
				kv.setTheKey(APP_STATE_LAST_SERVER_ID);
			}
			kv.setTheValue(getCurrentServer().getId() + "");
			keyValueInDbService.save(kv);
		}
	}

	private boolean addServer(Server server) {
		if (server == null) {
			return false;
		}
		boolean exists = getServers().stream().anyMatch(b -> b.getHost().equalsIgnoreCase(server.getHost()));
		if (!exists) {
			this.servers.add(server);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean removeServer(Server server) {
		if (server == null) {
			return false;
		}
		Optional<Server> svOp = getServers().stream().filter(b -> b.getHost().equalsIgnoreCase(server.getHost())).findAny();
		if (svOp.isPresent()) {
			this.servers.remove(svOp.get());
			return true;
		} else {
			return false;
		}
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

	public Server getCurrentServer() {
		return currentServer;
	}

	public void setCurrentServer(Server cs) {
		if (cs != null) {
			if (currentServer == null || (int)cs.getId() != (int)currentServer.getId() ) {
				currentServer = cs;
				fireSwitchEvent();
			}
		} else {
			currentServer = null;
			fireSwitchEvent();
		}
	}

	public List<Server> getServers() {
		return servers;
	}

	public void setServers(List<Server> servers) {
		this.servers = servers;
	}
	
	@EventListener
	public void whenServerCreated(ModelCreatedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server sv = serverCreatedEvent.getModel();
		addServer(sv);
	}
	
	@EventListener
	public void whenServerDeleted(ModelDeletedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server sv = serverCreatedEvent.getModel();
		removeServer(sv);
		
		if (currentServer != null && sv.getId().equals(currentServer.getId())) {
			setCurrentServer(null);
		}
	}

}
