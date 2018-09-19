package com.go2wheel.mysqlbackup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.go2wheel.mysqlbackup.value.OsTypeWrapper;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.google.common.collect.Lists;

public class MysqlInstance extends BaseModel {
	
	public static final String FIXED_DUMP_FILE_NAME = "mysqldump.sql";
	public static final String DEFAULT_CLIENT_BIN = "mysql";

	public static String getDefaultDumpFileName(String os) {
		if (OsTypeWrapper.of(os).isWin()) {
			return "x:/tmp/mysqldump.sql";
		} else {
			return "/tmp/mysqldump.sql";
		}
	}
	
	public static String getDefaultRestartCmd(String os) {
		if (OsTypeWrapper.of(os).isWin()) {
			return "Restart-Service wampmysqld64"; 
		} else {
			return "systemctl restart mysqld";
		}
	}
	
	private String host = "localhost";
	private int port = 3306;
	@NotEmpty(message = CommonMessageKeys.VALIDATOR_NOTNULL)
	private String username;
	private String password;
	private String mycnfFile;
	
	@NotEmpty
	private String dumpFileName;
	
	@NotEmpty
	private String clientBin;
	
	@NotEmpty
	private String restartCmd;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String flushLogCron;
	
	@NotNull(message=CommonMessageKeys.VALIDATOR_NOTNULL)
	private Integer serverId;
	
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
		return getMysqlSettings() != null && getMysqlSettings().size() > 0 && getLogBinSetting().isEnabled();
	}

	public String getFlushLogCron() {
		return flushLogCron;
	}

	public void setFlushLogCron(String flushLogCron) {
		this.flushLogCron = flushLogCron;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, fields);
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
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
		private final String dumpFileName;
		private final String restartCmd;
		
		private final String clientBin;
		
		
		private Set<String> mysqlSettings = new HashSet<>();
		
		private final int serverId;
		
		public MysqlInstanceBuilder(Server server, String password, String clientBin) {
			super();
			this.serverId = server.getId();
			this.password = password;
			this.clientBin = clientBin;
			this.dumpFileName = getDefaultDumpFileName(server.getOs());
			this.restartCmd = getDefaultRestartCmd(server.getOs());
		}

		public MysqlInstanceBuilder(int serverId, String password,String clientBin, String dumpFilename, String restartCmd) {
			super();
			this.serverId = serverId;
			this.password = password;
			this.clientBin = clientBin;
			this.dumpFileName = dumpFilename;
			this.restartCmd = restartCmd;
		}
		
		public MysqlInstanceBuilder withUsername(String username) {
			this.username = username;
			return this;
		}
		
		public MysqlInstanceBuilder addSetting(String key, String value) {
			this.mysqlSettings.add(key + "=" + value);
			return this;
		}

		
		public MysqlInstanceBuilder withHost(String host) {
			this.host = host;
			return this;
		}
		
		public MysqlInstanceBuilder withMycnfFile(String mycnfFile) {
			this.mycnfFile = mycnfFile;
			return this;
		}
		
		public MysqlInstanceBuilder withPort(int port) {
			this.port = port;
			return this;
		}
		
//		public MysqlInstanceBuilder withDumpFileName(String dumpFileName) {
//			this.dumpFileName = dumpFileName;
//			return this;
//		}

		
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
			mi.setDumpFileName(dumpFileName);
			mi.setMysqlSettings(new ArrayList<>(mysqlSettings));
			mi.setHost(host);
			mi.setRestartCmd(restartCmd);
			mi.setClientBin(clientBin);
			return mi;
		}
	}

	public MysqlVariables getLogBinSetting() {
		return new MysqlVariables(mysqlSettings);
	}

	public void setLogBinSetting(MysqlVariables logBinSetting) {
		if (logBinSetting == null) {
			this.mysqlSettings = Lists.newArrayList();
		} else {
			this.mysqlSettings = logBinSetting.toLines();
		}
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
		this.clientBin = clientBin;
	}

	public String getRestartCmd() {
		return restartCmd;
	}

	public void setRestartCmd(String restartCmd) {
		this.restartCmd = restartCmd;
	}

}
