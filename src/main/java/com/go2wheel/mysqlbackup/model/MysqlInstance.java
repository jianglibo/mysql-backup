package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class MysqlInstance extends BaseModel {
	
	public static final String VAR_DATADIR = "datadir";
	
	private String host;
	private int port;
	private String username;
	private String password;
	private String mycnfFile;
	
	private String dumpFileName;
	private String clientBin;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String flushLogCron;
	
	private int serverId;
	
	private List<String> mysqlSettings = new ArrayList<>();
	
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


	public boolean isReadyForBackup() {
		return getMysqlSettings().size() > 0;
	}

	public String getFlushLogCron() {
		return flushLogCron;
	}

	public void setFlushLogCron(String flushLogCron) {
		this.flushLogCron = flushLogCron;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return null;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<String> getMysqlSettings() {
		return mysqlSettings;
	}

	public void setMysqlSettings(List<String> mysqlSettings) {
		this.mysqlSettings = mysqlSettings;
	}

	public static class MysqlInstanceBuilder {
		private String host = "localhost";
		private int port = 3306;
		private String username = "root";
		private final String password;
		private String mycnfFile;
		private String flushLogCron;
		
		private Set<String> mysqlSettings = new HashSet<>();
		
		private final int serverId;

		public MysqlInstanceBuilder(int serverId, String password) {
			super();
			this.serverId = serverId;
			this.password = password;
		}
		
		public MysqlInstanceBuilder withUsername(String username) {
			this.username = username;
			return this;
		}
		
		public MysqlInstanceBuilder addSetting(String key, String value) {
			this.mysqlSettings.add(key + "|" + value);
			return this;
		}

		
		public MysqlInstanceBuilder withHost(String host) {
			this.host = host;
			return this;
		}
		
		public MysqlInstanceBuilder withPort(int port) {
			this.port = port;
			return this;
		}

		
		public MysqlInstanceBuilder withFlushLogCron(String flushLogCron) {
			this.flushLogCron = flushLogCron;
			return this;
		}
		
		public MysqlInstance build() {
			MysqlInstance mi = new MysqlInstance();
			mi.setCreatedAt(new Date());
			mi.setFlushLogCron(flushLogCron);
			mi.setMycnfFile(mycnfFile);
			mi.setPassword(password);
			mi.setPort(port);
			mi.setServerId(serverId);
			mi.setUsername(username);
			mi.setMysqlSettings(new ArrayList<>(mysqlSettings));
			mi.setHost(host);
			return mi;
		}
	}

	public LogBinSetting getLogBinSetting() {
		return new LogBinSetting(mysqlSettings);
	}

	public void setLogBinSetting(LogBinSetting logBinSetting) {
		this.mysqlSettings = logBinSetting.toLines();
	}

	public String getDumpFileName() {
		return dumpFileName;
	}

	public void setDumpFileName(String dumpFileName) {
		this.dumpFileName = dumpFileName;
	}

	public String getClientBin() {
		return clientBin;
	}

	public void setClientBin(String clientBin) {
		if (clientBin != null && !clientBin.endsWith("/")) {
			clientBin = clientBin + "/";
		}
		this.clientBin = clientBin;
	}

}
