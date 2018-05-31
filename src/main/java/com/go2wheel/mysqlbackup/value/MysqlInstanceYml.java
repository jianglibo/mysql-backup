package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class MysqlInstanceYml {
	
	public static final String VAR_DATADIR = "datadir";
	
	private int port = 3306;
	private String username;
	private String password;
	private String mycnfFile;
	
	private String flushLogCron;
	
	private boolean readyForBackup;
	
	private LogBinSetting logBinSetting;
	
	@Override
	public String toString() {
		return YamlInstance.INSTANCE.yaml.dumpAsMap(this);
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getMycnfFile() {
		return mycnfFile;
	}
	public void setMycnfFile(String mycnfFile) {
		this.mycnfFile = mycnfFile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}
	
	public String getUsername(String inCaseNotExists) {
		if (StringUtil.hasAnyNonBlankWord(getUsername())) {
			return getUsername();
		} else {
			return inCaseNotExists;
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LogBinSetting getLogBinSetting() {
		return logBinSetting;
	}

	public void setLogBinSetting(LogBinSetting logBinSetting) {
		this.logBinSetting = logBinSetting;
	}

	public boolean isReadyForBackup() {
		return readyForBackup;
	}

	public void setReadyForBackup(boolean readyForBackup) {
		this.readyForBackup = readyForBackup;
	}

	public String getFlushLogCron() {
		return flushLogCron;
	}

	public void setFlushLogCron(String flushLogCron) {
		this.flushLogCron = flushLogCron;
	}

}
