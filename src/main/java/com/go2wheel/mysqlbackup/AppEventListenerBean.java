package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.text.ParseException;

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
import com.go2wheel.mysqlbackup.service.ReuseableCronService;

@Component
public class AppEventListenerBean {
	
	@Autowired
	private ReuseableCronService reuseableCronService;

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
    	long crons = reuseableCronService.count();
    	if (crons == 0) {
    		String s = "0 0 7 * * ?";
    		try {
				new CronExpression(s);
				ReusableCron rc = new ReusableCron(s, "每天早上7点整。");
				reuseableCronService.save(rc);
			} catch (ParseException e) {
				e.printStackTrace();
			}
    	}
    	logger.info("onApplicationStartedEvent be called.");
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
