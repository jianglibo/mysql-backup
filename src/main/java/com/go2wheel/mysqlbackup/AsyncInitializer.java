package com.go2wheel.mysqlbackup;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AsyncInitializer {

	@Autowired
	private ConfigFileWatcher configFileWatcher;
	
	@PostConstruct
	public void post() {
		configFileWatcher.watch();
	}
}
