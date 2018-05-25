package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.UpgradeUtil;

@Component
public class AppEventListenerBean {

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
//    	myAppSettings.post();
    }
    
    @EventListener
    public void onApplicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
    	Path upgrade = Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE);
    	
    	if (Files.exists(upgrade)) {
    		try {
				Files.delete(upgrade);
				System.exit(BackupCommand.RESTART_CODE);
			} catch (IOException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
    	}
    	
    }
    
    
    
}
