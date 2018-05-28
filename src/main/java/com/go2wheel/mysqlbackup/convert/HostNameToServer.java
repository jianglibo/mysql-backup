package com.go2wheel.mysqlbackup.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerService;

@Component
public class HostNameToServer implements Converter<String, Server> {
	
	@Autowired
	private ServerService serverService;

	@Override
	public Server convert(String source) {
		return serverService.findByHost(source);
	}

}
