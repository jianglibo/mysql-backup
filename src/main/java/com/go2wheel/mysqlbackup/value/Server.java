package com.go2wheel.mysqlbackup.value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.util.BomUtil;
import com.go2wheel.mysqlbackup.util.FileUtil;

public class Server {

	private String host;

	private String name;

	private int coreNumber;
	private String mem;

	private String os;

	private String username = "root";

	private final ObjectMapper objectMapper;

	private List<ConfigFile> configFiles = new ArrayList<>();

	private int loadValve = 70;
	private int memoryValve = 70;
	private int diskValve = 70;

	public <T extends PsLogBase> List<T> getLogResult(String appName, String cmdKey, Class<T> clazz, int num)
			throws JsonParseException, JsonMappingException, IOException {
		Optional<ConfigFile> cfop = getConfigFiles().stream().filter(cf -> cf.getAppName().equals(appName)).findAny();
		List<T> results = new ArrayList<>();
		if (cfop.isPresent()) {
			Path logf = cfop.get().getLogDirs().get(cmdKey);
			if (logf != null) {
				File[] files = FileUtil.getNewestFiles(logf, num);
				int fn = files.length;
				if (num > fn) {
					num = fn;
				}
				for (int i = 0; i < num; i++) {
					File f = files[i];
					byte[] bytes = Files.readAllBytes(f.toPath());
					String content = BomUtil.removeBom(bytes).toString();
					T result = objectMapper.readValue(content, clazz);
					long t = f.lastModified();
					result.setCreatedAt(new Date(t));
					results.add(result);
				}
			}
		}
		return results;
	}
	
	public List<PsDiskMemFreeResult> getMemoryFreeResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("borg", "memoryfree", PsDiskMemFreeResult.class, num);
	}

	public List<PsDiskMemFreeResult> getDiskFreeResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("borg", "diskfree", PsDiskMemFreeResult.class, num);
	}

	public List<PsBorgAchiveResult> getBorgArchiveResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("borg", "archive", PsBorgAchiveResult.class, num);
	}

	public List<PsBorgAchiveResult> getBorgPruneResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("borg", "prune", PsBorgAchiveResult.class, num);
	}

	public List<PsMysqldumpResult> getMysqlDumpResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("mysql", "dump", PsMysqldumpResult.class, num);
	}

	public List<PsMysqlflushResult> getMysqlFlushResult(int num)
			throws JsonParseException, JsonMappingException, IOException {
		return getLogResult("mysql", "flushlog", PsMysqlflushResult.class, num);
	}

	public Server(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getCoreNumber() {
		return coreNumber;
	}

	public void setCoreNumber(int coreNumber) {
		this.coreNumber = coreNumber;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public boolean supportSSH() {
		if (getOs() == null) {
			return true;
		}
		return getOs().contains("linux");
	}

	public int getLoadValve() {
		return loadValve;
	}

	public void setLoadValve(int loadValve) {
		this.loadValve = loadValve;
	}

	public int getMemoryValve() {
		return memoryValve;
	}

	public void setMemoryValve(int memoryValve) {
		this.memoryValve = memoryValve;
	}

	public int getDiskValve() {
		return diskValve;
	}

	public void setDiskValve(int diskValve) {
		this.diskValve = diskValve;
	}

	public List<ConfigFile> getConfigFiles() {
		return configFiles;
	}

	public void setConfigFiles(List<ConfigFile> configFiles) {
		this.configFiles = configFiles;
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}
}
