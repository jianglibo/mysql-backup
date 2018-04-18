package com.go2wheel.mysqlbackup.value;

public class MysqlInstance {
	
	private String host;
	
	private int sshPort = 22;
	
	private int mysqlPort = 3306;
	
	private String username;
	private String password;
	
	private String mycnfFile;
	
	private String sshKeyFile;
	
	@Override
	public String toString() {
		return String.format("[host: %s:%s, username: %s, password: %s]", getHost(), getSshPort(), getUsername(), getPassword() == null ? "" : getPassword().replaceAll(".", "*"));
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public int getMysqlPort() {
		return mysqlPort;
	}

	public void setMysqlPort(int mysqlPort) {
		this.mysqlPort = mysqlPort;
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
	public String getMycnfFile() {
		return mycnfFile;
	}
	public void setMycnfFile(String mycnfFile) {
		this.mycnfFile = mycnfFile;
	}
	public String getSshKeyFile() {
		return sshKeyFile;
	}
	public void setSshKeyFile(String sshKeyFile) {
		this.sshKeyFile = sshKeyFile;
	}
}
