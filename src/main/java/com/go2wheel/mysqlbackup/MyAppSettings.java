package com.go2wheel.mysqlbackup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "myapp")
@Component
public class MyAppSettings {
	
	private SshConfig ssh;
	
	
	public SshConfig getSsh() {
		return ssh;
	}


	public void setSsh(SshConfig ssh) {
		this.ssh = ssh;
	}
	
	public static class SshConfig {
		private String sshIdrsa;
		private String knownHosts;

		public String getSshIdrsa() {
			return sshIdrsa;
		}
		public void setSshIdrsa(String sshIdrsa) {
			this.sshIdrsa = sshIdrsa;
		}
		public String getKnownHosts() {
			return knownHosts;
		}
		public void setKnownHosts(String knownHosts) {
			this.knownHosts = knownHosts;
		}
	}


}
