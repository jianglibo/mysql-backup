package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.ReusableCron;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.service.ReuseableCronService;
import com.go2wheel.mysqlbackup.service.ServerGrpService;
import com.go2wheel.mysqlbackup.value.DefaultValues;

@Component
public class AppEventListenerBean {
	
	@Autowired
	private ReuseableCronService reuseableCronService;
	
	@Autowired
	private ServerGrpService serverGrpService;
	
	@Autowired
	private DefaultValues defaultValues;

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
    				if (reuseableCronService.findByExpression(cr.getExpression()) == null) {
    					reuseableCronService.save(cr);
    				}
    			});
    	createDefaultServerGrp();
    	logger.info("onApplicationStartedEvent be called.");
    }
    
    
    private void createDefaultServerGrp() {
    	ServerGrp sg = serverGrpService.findByEname("default");
    	if (sg == null) {
    		sg = new ServerGrp("default");
    		serverGrpService.save(sg);
    	}
    }
    
//    @EventListener
//    public void onApplicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
//    	Path upgrade = Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE);
//    	
//    	if (Files.exists(upgrade)) {
//    		try {
//				Files.delete(upgrade);
//				System.exit(BackupCommand.RESTART_CODE);
//			} catch (IOException e) {
//				ExceptionUtil.logErrorException(logger, e);
//			}
//    	}
//    	
//    }
    
    
    
}
