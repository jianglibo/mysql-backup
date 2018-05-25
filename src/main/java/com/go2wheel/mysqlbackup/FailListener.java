package com.go2wheel.mysqlbackup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;

public class FailListener implements ApplicationListener<ApplicationFailedEvent>, ApplicationContextAware {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ApplicationContext applicationContext;
	
	@Override
	public void onApplicationEvent(ApplicationFailedEvent event) {
		Throwable t = event.getException();
		logger.error(("active profiles: " + String.join(",", applicationContext.getEnvironment().getActiveProfiles())));
		ExceptionUtil.logThrowable(logger, t);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
