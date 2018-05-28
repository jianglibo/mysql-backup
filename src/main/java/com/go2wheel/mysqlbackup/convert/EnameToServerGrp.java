package com.go2wheel.mysqlbackup.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.service.ServerGrpService;

@Component
public class EnameToServerGrp implements Converter<String, ServerGrp> {
	
	@Autowired
	private ServerGrpService serverGrpService;

	@Override
	public ServerGrp convert(String source) {
		return serverGrpService.findByEname(source);
	}

}
