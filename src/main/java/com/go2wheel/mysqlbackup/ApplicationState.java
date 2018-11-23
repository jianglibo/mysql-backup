package com.go2wheel.mysqlbackup;

import java.text.ParseException;
import java.util.Locale;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.event.ServerSwitchEvent;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class ApplicationState {

	public static final String APPLICATION_STATE_PERSIST_FILE = "application-state.yml";
	
	public static boolean IS_PROD_MODE = false;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	private Locale local = Locale.CHINESE;
	
	private Server currentServer;

	private FacadeResult<?> facadeResult;

	private CommandStepState step = CommandStepState.INIT_START;
	
	@Value("${expectit.echo}")
	private boolean expectitEcho;
	
	public static enum CommandStepState {
		INIT_START, WAITING_SELECT, BOX_SELECTED
	}
	
	public void fireSwitchEvent() {
		ServerSwitchEvent sce = new ServerSwitchEvent(this);
		applicationEventPublisher.publishEvent(sce);
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
