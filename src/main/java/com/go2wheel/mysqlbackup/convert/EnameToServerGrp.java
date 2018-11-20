package com.go2wheel.mysqlbackup.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.dbservice.ServerGrpDbService;
import com.go2wheel.mysqlbackup.model.ServerGrp;

@Component
public class EnameToServerGrp implements Converter<String, ServerGrp> {
	
	@Autowired
	private ServerGrpDbService serverGrpDbService;

	@Override
	public ServerGrp convert(String source) {
		return serverGrpDbService.findByEname(source);
	}

}
