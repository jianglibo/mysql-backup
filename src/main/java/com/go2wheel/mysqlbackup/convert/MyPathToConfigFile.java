package com.go2wheel.mysqlbackup.convert;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import com.go2wheel.mysqlbackup.service.ConfigFileLoader;
import com.go2wheel.mysqlbackup.value.ConfigFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MyPathToConfigFile implements Converter<String, ConfigFile> {

	@Autowired
	private ConfigFileLoader configFileLoader;

	@Override
	public ConfigFile convert(String source) {
		if (!Files.exists(Paths.get(source))) {
			return null;
		}
		return configFileLoader.getOne(source);
	}
}
