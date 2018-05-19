package com.go2wheel.mysqlbackup.value;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class Box {
	
	public static final String NO_PASSWORD="i_have_no_password";
	
	public static final String NO_SSHKEY_FILE="i_have_no_sshkey_file";
	
	public static final String ROLE_SOURCE="SOURCE";
	
	public static final String ROLE_DEST="DEST";
	
	private String host;
	private int port = 22;
	
	private String fingerprint;
	
	private String username = "root";
	private String password = NO_PASSWORD;
	
	private String sshKeyFile;
	
	private MysqlInstance mysqlInstance;
	
	private BorgBackupDescription borgBackup;
	
	private String role = ROLE_SOURCE;
	
	@Override
	public String toString() {
		return YamlInstance.INSTANCE.yaml.dumpAsMap(this);
	}
	
	public boolean hasFingerPrint() {
		return fingerprint != null && !fingerprint.trim().isEmpty();
	}
	
	public boolean canSShKeyAuth() {
		boolean b =  sshKeyFile != null && !sshKeyFile.trim().isEmpty() && Files.exists(Paths.get(sshKeyFile));
		return b;
	}
	
	public boolean canPasswordAuth() {
		return StringUtil.hasAnyNonBlankWord(getPassword()) && !NO_PASSWORD.equals(getPassword());
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
	
	public String getUsername(String inCaseNotExists) {
		if (StringUtil.hasAnyNonBlankWord(getUsername())) {
			return getUsername();
		} else {
			return inCaseNotExists;
		}
	}

	public void setUsername(String username) {;
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


	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public BorgBackupDescription getBorgBackup() {
		return borgBackup;
	}

	public void setBorgBackup(BorgBackupDescription borgBackup) {
		this.borgBackup = borgBackup;
	}
	
	

}
