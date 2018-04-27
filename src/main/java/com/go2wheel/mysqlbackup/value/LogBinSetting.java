package com.go2wheel.mysqlbackup.value;

import java.util.HashMap;
import java.util.Map;

public class LogBinSetting {
	
	// host_name-bin
	public static final String LOG_BIN = "log_bin";
	//	Holds the base name and path for the binary log files, which can be set with the --log-bin server option. In MySQL 5.7, the default base name is the name of the host machine with the suffix -bin. The default location is the data directory.
	public static final String LOG_BIN_BASENAME = "log_bin_basename";
	public static final String LOG_BIN_INDEX = "log_bin_index";

	
	private Map<String, String> map = new HashMap<>();
	
	public LogBinSetting() {
	}

	public LogBinSetting(Map<String, String> map) {
		super();
		this.map = map;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public boolean isEnabled() {
		return map.containsKey(LOG_BIN) && "ON".equals(map.get(LOG_BIN));
	}
	
}
