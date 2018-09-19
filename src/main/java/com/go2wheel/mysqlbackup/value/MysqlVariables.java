package com.go2wheel.mysqlbackup.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class MysqlVariables {
	
	// host_name-bin
	public static final String LOG_BIN_VARIABLE = "log_bin";
	//	Holds the base name and path for the binary log files, which can be set with the --log-bin server option. In MySQL 5.7, the default base name is the name of the host machine with the suffix -bin. The default location is the data directory.
	public static final String LOG_BIN_BASENAME = "log_bin_basename";
	public static final String LOG_BIN_INDEX = "log_bin_index";
	public static final String DATA_DIR = "datadir";
	public static final String SOCKET = "socket";
	public static final String VERSION = "version";

	
	private Map<String, String> map = new HashMap<>();
	
	@Override
	public String toString() {
		return map.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(", ", "[", "]"));
	}
	
	public MysqlVariables() {
	}
	
	public MysqlVariables(Map<String, String> map) {
		super();
		this.map = map;
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public MysqlVariables(List<String> lines) {
		super();
		Map<String, String> m = StringUtil.toPair(lines);
		this.map.putAll(m);
	}

	
	public List<String> toLines() {
		return StringUtil.toLines(this.map);
	}
	
	public String getDataDirEndWithPathSeparator() {
		String s = map.get(DATA_DIR);
		char pathSeparator = s.indexOf('\\') == -1 ? '/' : '\\';
		if (s != null && !s.endsWith(pathSeparator + "")) {
			s = s + pathSeparator;
		}
		return s;
	}
	
	public String getDataDirEndNoPathSeparator() {
		String s = map.get(DATA_DIR);
		char pathSeparator = s.indexOf('\\') == -1 ? '/' : '\\';
		if (s != null && s.endsWith(pathSeparator + "")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	public String getLogBinDirWithEndingPathSeparator() {
		String s = getLogBinBasename();
		return PathUtil.getParentWithEndingSeparator(s);
	}
	
	
	public String getLogBinBasenameOnlyName() {
		String s = map.getOrDefault(LOG_BIN_BASENAME, "");
		return PathUtil.getFileName(s);
	}
	
	public String getLogBinIndexNameOnly() {
		String s = map.getOrDefault(LOG_BIN_INDEX, "");
		return PathUtil.getFileName(s);
	}

	
	public String getLogBinBasename() {
		return map.getOrDefault(LOG_BIN_BASENAME, "");
	}
	
	public String getVersion() {
		return map.getOrDefault(VERSION, "");
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
	
	public static class MysqlVersionWrapper {
		
		private String version;
		
		public static MysqlVersionWrapper of(String version) {
			return new MysqlVersionWrapper(version);
		}
		
		// 5.5.24-log
		public boolean isAfter55() {
			int s = Integer.valueOf(getVersion().substring(0, 3).replace(".", ""));
			return s > 55;
		}
		
		private MysqlVersionWrapper(String version) {
			this.setVersion(version);
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

	}
	
}
