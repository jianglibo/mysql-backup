package com.go2wheel.mysqlbackup;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class LocaledMessageService {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ApplicationState applicationState;
	
	public String getMessage(String code, Object...args) {
		return messageSource.getMessage(code, args, applicationState.getLocal());
	}

}
