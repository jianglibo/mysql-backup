package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class Server extends BaseModel {
	
	public static final String NO_PASSWORD="i_have_no_password";
	
	public static final String NO_SSHKEY_FILE="i_have_no_sshkey_file";
	
	public static final String ROLE_SOURCE="SOURCE";
	
	public static final String ROLE_DEST="DEST";
	
	@NotNull
	private String host;
	
	private int coreNumber;
	
	private int port = 22;
	
	private String username = "root";
	private String password = NO_PASSWORD;
	
	private String sshKeyFile = NO_SSHKEY_FILE;
	
	private MysqlInstance mysqlInstance;
	
	private BorgDescription borgDescription;
	
	public Server() {}
	
	public Server(String host) {
		this.host = host;
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
		return ObjectUtil.toListRepresentation(this, "id", "host");
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
	
}
