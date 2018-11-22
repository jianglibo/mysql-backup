package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.job.PowershellCommandSchedule;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.value.ConfigFile;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ConfigFileLoader {

	private LoadingCache<String, ConfigFile> cache;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private MyAppSettings myAppSettings;
	
	@Autowired
	private PowershellCommandSchedule powershellCommandSchedule;

	@PostConstruct
	private void post() {
		cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, ConfigFile>() {
			public ConfigFile load(String key) throws JsonParseException, JsonMappingException, IOException {
				return loadOne(key);
			}
		});
	}

	private ConfigFile loadOne(String configFileName) throws JsonParseException, JsonMappingException, IOException {
		ConfigFile cf = objectMapper.readValue(Files.readAllBytes(Paths.get(configFileName)), ConfigFile.class);
		cf.setMypath(configFileName);
		Map<String, List<String>> cmdSegmentsMap = new HashMap<>();
		cf.getTaskcmd().entrySet().forEach(entry -> {
			String k = entry.getKey();
			String v = entry.getValue();
			String entryPoint = myAppSettings.getPsappPath().resolve(cf.getEntryPoint()).toAbsolutePath().normalize().toString();
			v = String.format(v, entryPoint, cf.getMypath());
			List<String> cmdSegments = new ArrayList<>();
			cmdSegments.add(myAppSettings.getPowershell());
			cmdSegments.addAll(Arrays.asList(v.split("\\s+")));
			cmdSegmentsMap.put(k, cmdSegments);
		});
		cf.setProcessBuilderNeededList(cmdSegmentsMap);
		return cf;
	}

	public ConfigFile getOne(String configFileName) throws ExecutionException {
		return cache.get(configFileName);
	}
	
	public void scheduleAll() throws SchedulerException, ParseException {
		for(ConfigFile configFile: cache.asMap().values()) {
			powershellCommandSchedule.schedule(configFile);
		}
	}
	
	public ProcessExecResult runCommand(String configFileName, String psCmdKey) throws ExecutionException {
		return PSUtil.invokePowershell(cache.get(configFileName).getProcessBuilderNeededList().get(psCmdKey), myAppSettings.getConsoleCharset());
	}
}
