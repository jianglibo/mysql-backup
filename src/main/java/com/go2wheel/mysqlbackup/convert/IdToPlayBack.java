package com.go2wheel.mysqlbackup.convert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.service.UserGroupLoader;
import com.go2wheel.mysqlbackup.value.Subscribe;

@Component
public class IdToPlayBack implements Converter<String, Subscribe> {
	
	@Autowired
	private UserGroupLoader playBackDbService;

	@Override
	public Subscribe convert(String source) {
			return playBackDbService.getSubscribeById(source);
	}

}
