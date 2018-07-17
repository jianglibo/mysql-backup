package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.util.Arrays;

import org.quartz.CronExpression;
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

import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.value.DefaultValues;

@Component
public class AppEventListenerBean implements EnvironmentAware {
	
	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private DefaultValues defaultValues;
	
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
    	defaultValues.getCron().getCommon().stream()
    			.map(s -> s.split("\\|", 2))
    			.filter(ss -> ss.length == 2)
    			.map(ss -> new ReusableCron(ss[0].trim(), ss[1].trim()))
    			.filter(cr -> {
    				try {
    					new CronExpression(cr.getExpression());
    					return true;
    				} catch (Exception e) {
    					return false;
					}
    			})
    			.forEach(cr -> {
    				if (reuseableCronDbService.findByExpression(cr.getExpression()) == null) {
    					reuseableCronDbService.save(cr);
    				}
    			});
    	createDefaultServerGrp();
    	createServerMyself();
    	ApplicationState.IS_PROD_MODE = !Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> "dev".equals(p));
    	logger.info("onApplicationStartedEvent be called.");
    }
    
	private void createServerMyself() {
		Server server = serverDbService.findByHost("localhost");
		if (server == null) {
			server = new Server("localhost", "localhost");
			server.setOs("win");
			server.setServerStateCron(defaultValues.getCron().getServerState());
			server.setStorageStateCron(defaultValues.getCron().getStorageState());
			serverDbService.save(server);
		}
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
