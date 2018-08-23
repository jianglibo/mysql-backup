package com.go2wheel.mysqlbackup.model;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.constraints.NotEmpty;

import com.go2wheel.mysqlbackup.annotation.OstypeIndicator;
import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;

public class Server extends BaseModel {
	
	public static final String NO_PASSWORD="i_have_no_password";
	
	public static final String NO_SSHKEY_FILE="i_have_no_sshkey_file";
	
	public static final String ROLE_GET="GET";
	
	public static final String ROLE_SET="SET";
	
	@NotEmpty
	private String host;
	
	@NotEmpty
	private String name;
	
	private int coreNumber;
	
	private int port = 22;
	
	/**
	 * os的格式。
	 * linux_centos_7_xx
	 * win_xp_
	 * win_10_
	 * win_2000_
	 * 
	 */
	@OstypeIndicator
	private String os;
	
	private String username = "root";
	private String password = NO_PASSWORD;
	
	private String sshKeyFile = NO_SSHKEY_FILE;
	
	private MysqlInstance mysqlInstance;
	
	private BorgDescription borgDescription;
	
	@NotEmpty
	private String serverRole = ROLE_GET;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String serverStateCron;
	
	@CronExpressionConstraint(allowEmpty=true)
	private String storageStateCron;
	
	private int loadValve = 70;
	private int memoryValve = 70;
	private int diskValve = 70;
	
	public Server() {}
	
	public Server(String host, String name) {
		this.host = host;
		this.name = name;
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
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSshKeyFile() {
		return sshKeyFile;
	}

	public void setSshKeyFile(String sshKeyFile) {
		this.sshKeyFile = sshKeyFile;
	}

	@Override
	public String toListRepresentation(String... fields) {
		return ObjectUtil.toListRepresentation(this, "id","name", "host");
	}

	public MysqlInstance getMysqlInstance() {
		return mysqlInstance;
	}

	public void setMysqlInstance(MysqlInstance mysqlInstance) {
		this.mysqlInstance = mysqlInstance;
	}

	public BorgDescription getBorgDescription() {
		return borgDescription;
	}

	public void setBorgDescription(BorgDescription borgDescription) {
		this.borgDescription = borgDescription;
	}

	public boolean canSShKeyAuth() {
		boolean b =  sshKeyFile != null && !sshKeyFile.trim().isEmpty() && Files.exists(Paths.get(sshKeyFile.trim()));
		return b;
	}
	
	public boolean canPasswordAuth() {
		return StringUtil.hasAnyNonBlankWord(getPassword()) && !NO_PASSWORD.equals(getPassword());
	}

	public String getServerRole() {
		return serverRole;
	}

	public void setServerRole(String serverRole) {
		this.serverRole = serverRole;
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

	public String getServerStateCron() {
		return serverStateCron;
	}

	public void setServerStateCron(String serverStateCron) {
		this.serverStateCron = serverStateCron;
	}

	public String getStorageStateCron() {
		return storageStateCron;
	}

	public void setStorageStateCron(String storageStateCron) {
		this.storageStateCron = storageStateCron;
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

}
