package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class AppEventListenerBean implements EnvironmentAware {
	
	private Environment environment;
	
	public static boolean IS_PROD_MODE;
	
	private Locale local = Locale.CHINESE;
	
	private FacadeResult<?> facadeResult;
	
	@Autowired
	private MyAppSettings myAppSettings;
	
	@Autowired
	private ConfigFileLoader configFileLoader;

    private static final Logger logger = LoggerFactory.getLogger(AppEventListenerBean.class);
    
    /**
     *  @see ApplicationEvent
     *  
     * @param event
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
    }
    
    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent event) throws IOException, SchedulerException, ParseException {
    	IS_PROD_MODE = !Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> "dev".equals(p));
    	parsePsdataDir();
    	logger.info("onApplicationStartedEvent be called. active profile: {}", IS_PROD_MODE ? "prod" : "dev");
    }
    

	private void parsePsdataDir() throws IOException, SchedulerException, ParseException {
		Path psdataDir = myAppSettings.getPsappPath();
		Path configs = psdataDir.resolve("configs");
		if (!Files.exists(configs)) {
			Files.createDirectories(configs);
		}
		configFileLoader.loadAll(configs);
		configFileLoader.scheduleAll();
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
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
