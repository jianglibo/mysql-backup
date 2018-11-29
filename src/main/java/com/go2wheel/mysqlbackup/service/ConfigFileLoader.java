package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.exception.IncompatibleConfigurationError;
import com.go2wheel.mysqlbackup.exception.NoActionException;
import com.go2wheel.mysqlbackup.job.PowershellCommandSchedule;
import com.go2wheel.mysqlbackup.util.BomUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.value.ConfigFile;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ConfigFileLoader {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	private LoadingCache<String, ConfigFile> cache;

	private Pattern findActionPattern = Pattern.compile(".*-Action\\s+(\\S+).*", Pattern.CASE_INSENSITIVE);

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MyAppSettings myAppSettings;

	@Autowired
	private PowershellCommandSchedule powershellCommandSchedule;

	@PostConstruct
	private void post() {
		cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, ConfigFile>() {
			public ConfigFile load(String key)
					throws JsonParseException, JsonMappingException, IOException, IncompatibleConfigurationError {
				return loadOne(key);
			}
		});
	}
	
	public void clearCache() {
		this.cache.invalidateAll();
	}
	
	public List<ConfigFile> getByHostname(String hostname) {
		return this.cache.asMap().values().stream().filter(cf -> cf.getHostName().equals(hostname)).collect(Collectors.toList());
	}

	private ConfigFile loadOne(String configFileName)
			throws JsonParseException, JsonMappingException, IOException, IncompatibleConfigurationError {
		String content = BomUtil.removeBom(Files.readAllBytes(Paths.get(configFileName))).toString();
		ConfigFile cf = objectMapper.readValue(content, ConfigFile.class);
		cf.setMypath(configFileName);
		for (Map.Entry<String, String> entry : cf.getTaskcmd().entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			Matcher m = findActionPattern.matcher(v);
			if (m.matches()) {
				Path logPath = Paths.get(cf.getLogDir(), cf.getHostName(), cf.getAppName(), m.group(1));
				cf.getLogDirs().put(k, logPath);
			} else {
				String s = "%s in %s, taskcmd cannot find -action.";
				throw new IncompatibleConfigurationError(String.format(s, cf.getAppName(), cf.getHostName()));
			}

			String entryPoint = myAppSettings.getPsappPath().resolve(cf.getEntryPoint()).toAbsolutePath().normalize()
					.toString();
			v = String.format(v, entryPoint, cf.getMypath());
			List<String> cmdSegments = new ArrayList<>();
			cmdSegments.add(myAppSettings.getPowershell());
			cmdSegments.addAll(Arrays.asList(v.split("\\s+")));
			cf.getProcessBuilderNeededList().put(k, cmdSegments);
		}
		return cf;
	}

	public ConfigFile getOne(String configFileName) throws ExecutionException {
		return cache.get(configFileName);
	}

	public void loadAll(Path configsPath) throws IOException, ExecutionException {
		List<Path> allJsonFiles = Files.walk(configsPath).filter(p -> Files.isRegularFile(p))
				.filter(p -> p.getFileName().toString().endsWith(".json")).collect(Collectors.toList());
		for (Path p : allJsonFiles) {
			getOne(p.toAbsolutePath().normalize().toString());
		}
		;
	}
	
	public void runAllCmds() {
		for(ConfigFile cf: cache.asMap().values()) {
			for(List<String> commands: cf.getProcessBuilderNeededList().values()) {
				ProcessExecResult per = PSUtil.invokePowershell(commands, myAppSettings.getConsoleCharset());
				logger.debug(String.join("\n", per.getStdOut()));
				logger.debug(String.join("\n", per.getStdError()));
			}
		}
	}

	public void scheduleAll() throws SchedulerException, ParseException {
		for (ConfigFile configFile : cache.asMap().values()) {
			powershellCommandSchedule.schedule(configFile);
		}
	}

	public ProcessExecResult runCommand(String configFileName, String psCmdKey)
			throws ExecutionException, NoActionException {
		List<String> commands = cache.get(configFileName).getProcessBuilderNeededList().get(psCmdKey);
		if (commands == null) {
			throw new NoActionException(cache.get(configFileName), psCmdKey);
		}
		return PSUtil.invokePowershell(commands, myAppSettings.getConsoleCharset());
	}
}
