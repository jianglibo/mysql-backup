package com.go2wheel.mysqlbackup.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;

@Component
public class HostNameToServer implements Converter<String, Server> {
	
	@Autowired
	private ServerDbService serverDbService;

	@Override
	public Server convert(String source) {
		return serverDbService.findByHost(source);
	}

}
