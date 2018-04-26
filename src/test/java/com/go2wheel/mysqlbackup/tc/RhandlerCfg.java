package com.go2wheel.mysqlbackup.tc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.go2wheel.mysqlbackup.resulthandler.MyThrowableResultHandler;

@TestConfiguration
public class RhandlerCfg {
	
	@Bean
	public MyThrowableResultHandler myThrowableResultHandler() {
		return new MyThrowableResultHandler();
	}
	
}
