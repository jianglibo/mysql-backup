package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.util.Arrays;

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

import com.go2wheel.mysqlbackup.dbservice.ServerGrpDbService;
import com.go2wheel.mysqlbackup.model.ServerGrp;

@Component
public class AppEventListenerBean implements EnvironmentAware {
	
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	private Environment environment;

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
    public void onApplicationStartedEvent(ApplicationStartedEvent event) throws IOException {
    	createDefaultServerGrp();
    	ApplicationState.IS_PROD_MODE = !Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> "dev".equals(p));
    	logger.info("onApplicationStartedEvent be called.");
    }
    

	private void createDefaultServerGrp() {
    	ServerGrp sg = serverGrpDbService.findByEname("default");
    	if (sg == null) {
    		sg = new ServerGrp("default");
    		serverGrpDbService.save(sg);
    	}
    }

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
