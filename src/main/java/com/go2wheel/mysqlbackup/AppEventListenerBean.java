package com.go2wheel.mysqlbackup;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

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
import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

@Component
public class AppEventListenerBean implements EnvironmentAware {
	
	private static String CONFIG_TEMPLATE_FOLDER_NAME = "config-templates";
	
	private Environment environment;
	
	public static boolean IS_PROD_MODE;
	
	private Locale local = Locale.CHINESE;
	
	private FacadeResult<?> facadeResult;
	
	@Autowired
	private MyAppSettings myAppSettings;
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
	@Autowired
	private UserGroupLoader userGroupLoader;

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
    public void onApplicationStartedEvent(ApplicationStartedEvent event) throws Exception {
    	IS_PROD_MODE = !Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> "dev".equals(p));
    	parsePsdataDir();
    	logger.info("onApplicationStartedEvent be called. active profile: {}", IS_PROD_MODE ? "prod" : "dev");
    }

	private void parsePsdataDir() throws Exception {
		Path psdataDir = myAppSettings.getPsdataDirPath();
		Path configsDir = psdataDir.resolve("configs");
		
		Path groupsFile = myAppSettings.getGroupsFile();
		Path usersFile = myAppSettings.getUsersFile();
		Path adminsFile = myAppSettings.getAdminFile();
		Path subscribesFile = myAppSettings.getSubscribeFile();
		
		Stream.of(configsDir).forEach(dir -> {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		});
		
		Stream.of(groupsFile, usersFile, adminsFile, subscribesFile).forEach(file -> {
			try {
				createDemoFile(file);
			} catch (IOException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		});
		
		if (!myAppSettings.isNotautoload()) {
			loadData(configsDir, true, true);
		}
	}
	
	public void loadData(Path configsDir, boolean schedule, boolean reloadCache) throws Exception {
		if (configsDir == null) {
			Path psdataDir = myAppSettings.getPsdataDirPath();
			configsDir = psdataDir.resolve("configs");
		}
		configFileLoader.loadAll(configsDir, reloadCache);
		userGroupLoader.loadAll(reloadCache);
		if (schedule) {
			configFileLoader.scheduleAll();
			userGroupLoader.schuduleAllSubscribes();
		}
	}

	private void createDemoFile(Path file) throws IOException {
		if (!Files.exists(file)) {
			Path demo = myAppSettings.getPsappPath().resolve(CONFIG_TEMPLATE_FOLDER_NAME).resolve(file.getFileName());
			Files.copy(demo, file);
		}
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
