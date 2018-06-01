package com.go2wheel.mysqlbackup.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.RemotePathUtil;

public class LogBinSetting {
	
	// host_name-bin
	public static final String LOG_BIN_VARIABLE = "log_bin";
	//	Holds the base name and path for the binary log files, which can be set with the --log-bin server option. In MySQL 5.7, the default base name is the name of the host machine with the suffix -bin. The default location is the data directory.
	public static final String LOG_BIN_BASENAME = "log_bin_basename";
	public static final String LOG_BIN_INDEX = "log_bin_index";

	
	private Map<String, String> map = new HashMap<>();
	
	@Override
	public String toString() {
		return map.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(", ", "[", "]"));
	}
	
	public LogBinSetting() {
	}
	
	public LogBinSetting(Map<String, String> map) {
		super();
		this.map = map;
	}
	
	public LogBinSetting(List<String> lines) {
		super();
		lines.stream().map(line -> line.split("\\|", 2)).filter(ss -> ss.length == 2).forEach(ss -> {
			map.put(ss[0].trim(), ss[1].trim());
		});
	}

	
	public List<String> toLines() {
		return this.map.entrySet().stream().map(es -> es.getKey() + "|" + es.getValue()).collect(Collectors.toList());
	}
	
	public String getLogBinDirWithEndingSlash() {
		String s = getLogBinBasename();
		return RemotePathUtil.getParentWithEndingSlash(s);
	}
	
	
	public String getLogBinBasenameOnlyName() {
		String s = map.getOrDefault(LOG_BIN_BASENAME, "");
		return s.substring(s.lastIndexOf('/') + 1);
	}
	
	public String getLogBinIndexNameOnly() {
		String s = map.getOrDefault(LOG_BIN_INDEX, "");
		return s.substring(s.lastIndexOf('/') + 1);
	}

	
	public String getLogBinBasename() {
		return map.getOrDefault(LOG_BIN_BASENAME, "");
	}
	
	public String getLogBinIndex() {
		return map.getOrDefault(LOG_BIN_INDEX, "");
	}



	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public boolean isEnabled() {
		return map.containsKey(LOG_BIN_VARIABLE) && "ON".equals(map.get(LOG_BIN_VARIABLE));
	}
	
}
