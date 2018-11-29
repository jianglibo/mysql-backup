package com.go2wheel.mysqlbackup.convert;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.value.ServerGrp;

@Component
public class EnameToServerGrp implements Converter<String, ServerGrp> {
	
	@Autowired
	private UserGroupLoader serverGrpDbService;

	@Override
	public ServerGrp convert(String source) {
		try {
			return serverGrpDbService.getGroupByName(source);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
