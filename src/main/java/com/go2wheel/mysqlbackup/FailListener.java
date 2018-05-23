package com.go2wheel.mysqlbackup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

import com.go2wheel.mysqlbackup.util.ExceptionUtil;

public class FailListener implements ApplicationListener<ApplicationFailedEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void onApplicationEvent(ApplicationFailedEvent event) {
		Throwable t = event.getException();
		ExceptionUtil.logThrowable(logger, t);
	}

}
