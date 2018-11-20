package com.go2wheel.mysqlbackup;

import java.text.ParseException;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class ApplicationState {

	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	public static boolean IS_PROD_MODE = false;
	
	private static final Integer APP_STATE_ID = 0;
	private static final String APP_STATE_NAME = "APP_STATE_NAME";
	private static final String APP_STATE_LAST_SERVER_ID = "LAST_SERVER_ID";
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private Locale local = Locale.CHINESE;
	
	private Server currentServer;

	private FacadeResult<?> facadeResult;

	private CommandStepState step = CommandStepState.INIT_START;
	
	@Value("${expectit.echo}")
	private boolean expectitEcho;
	
	@PostConstruct
	public void post() {
//		KeyValueInDb kv = keyValueInDbService.findByIdNameKey(APP_STATE_ID, APP_STATE_NAME, APP_STATE_LAST_SERVER_ID);
//		if (kv != null) {
//			Server server = serverDbService.findById(kv.getTheValue());
//			if (server != null) {
//				setCurrentServer(serverDbService.loadFull(server));
//			}
//		}
	}

	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}
	
	public void fireSwitchEvent() {
		ServerSwitchEvent sce = new ServerSwitchEvent(this);
		persistState();
		applicationEventPublisher.publishEvent(sce);
	}
	
	public void persistState() {
//		if (getCurrentServer() != null) {
//			KeyValueInDb kv = keyValueInDbService.findByIdNameKey(APP_STATE_ID, APP_STATE_NAME, APP_STATE_LAST_SERVER_ID);
//			if (kv == null) {
//				kv = new KeyValueInDb();
//				kv.setObjectId(APP_STATE_ID);
//				kv.setObjectName(APP_STATE_NAME);
//				kv.setTheKey(APP_STATE_LAST_SERVER_ID);
//			}
//			kv.setTheValue(getCurrentServer().getId() + "");
//			keyValueInDbService.save(kv);
//		}
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
	
	@EventListener
	public void whenServerDeleted(ModelDeletedEvent<Server> serverCreatedEvent) throws SchedulerException, ParseException {
		Server sv = serverCreatedEvent.getModel();
		if (currentServer != null && sv.getId().equals(currentServer.getId())) {
			setCurrentServer(null);
		}
	}

}
