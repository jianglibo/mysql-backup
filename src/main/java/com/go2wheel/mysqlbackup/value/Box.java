package com.go2wheel.mysqlbackup.value;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Box {

	private String host;
	private int port = 22;
	
	private String fingerprint;
	
	private String username;
	private String password;
	
	private String sshKeyFile;
	
	private MysqlInstance mysqlInstance;
	
	public boolean hasFingerPrint() {
		return fingerprint != null && !fingerprint.trim().isEmpty();
				
	}
	
	public boolean canSShKeyAuth() {
		boolean b =  sshKeyFile != null && !sshKeyFile.trim().isEmpty() && Files.exists(Paths.get(sshKeyFile));
		return b;
	}
	
	public boolean canPasswordAuth() {
		return password != null && !password.trim().isEmpty();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public MysqlInstance getMysqlInstance() {
		return mysqlInstance;
	}

	public void setMysqlInstance(MysqlInstance mysqlInstance) {
		this.mysqlInstance = mysqlInstance;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
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
	
	
	
}
