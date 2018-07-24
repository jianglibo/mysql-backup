package com.go2wheel.mysqlbackup.resulthandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.result.ResultHandlerConfig;

/**
 * @see ResultHandlerConfig
 * @author admin
 *
 */
@Configuration
public class ResultHandlerConfigMine {


	@Bean
	public HasErrorIdAndMsgkeyResultHandler hasErrorIdAndMsgkeyResultHandler() {
		return new HasErrorIdAndMsgkeyResultHandler();
	}
	
	@Bean
	public FacadeResultHandler<?> facadeResultHandler() {
		return new FacadeResultHandler<>();
	}

}
