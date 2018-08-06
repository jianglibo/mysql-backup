package com.go2wheel.mysqlbackup.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

public class MycnfFileHolder extends BlockedPropertiesFileHolder {
	
	public static final String DEFAULT_LOG_BIN_BASE_NAME = "hm-log-bin";
	
	public static final String MYSQLD_BLOCK = "mysqld";
	
	public static final String MYSQLD_LOG_BIN_KEY = "log-bin";
	
	private String myCnfFile;
	
	private Map<String, String> variables;
	
	public MycnfFileHolder() {
		super(new ArrayList<>());
	}
	
	public MycnfFileHolder(List<String> lines) {
		super(lines);
	}
	
	public boolean enableBinLog() {
		return enableBinLog(DEFAULT_LOG_BIN_BASE_NAME);
	}
	
	public byte[] toByteArray() {
		return String.join("\n", getLines()).getBytes();
	}
	
	/**
	 * 
	 * @return true if changed or else false.
	 */
	public boolean enableBinLog(String filename) {
		Assert.hasText(filename, "log-bin value is a must.");
		ConfigValue cv = getConfigValue(MYSQLD_BLOCK, MYSQLD_LOG_BIN_KEY);
		return setConfigValue(cv, filename);
	}

	public boolean disableBinLog() {
		ConfigValue cv = getConfigValue(MYSQLD_BLOCK, MYSQLD_LOG_BIN_KEY);
		return commentOutConfigValue(cv);
	}

	public String getMyCnfFile() {
		return myCnfFile;
	}

	public void setMyCnfFile(String myCnfFile) {
		this.myCnfFile = myCnfFile;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public MysqlVariables getMysqlVariables() {
		return new MysqlVariables(variables);
	}
}
