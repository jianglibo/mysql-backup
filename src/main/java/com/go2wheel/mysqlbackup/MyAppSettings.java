package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.dbservice.KeyValueDbService;
import com.go2wheel.mysqlbackup.dbservice.KeyValueService;
import com.go2wheel.mysqlbackup.exception.AppConfigurationError;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;

@ConfigurationProperties(prefix = com.go2wheel.mysqlbackup.MyAppSettings.MYAPP_PREFIX)
@Component
public class MyAppSettings {
	
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MYAPP_PREFIX = "myapp";

	private SshConfig ssh;

	private Set<String> storageExcludes;

	private CacheTimes cache;
	
	private String psdataDir;
	
	private Path psdataDirPath;
	
	private String psapp;
	
	private Path psappPath;
	
	private String powershell;
	
	private String consoleCharsetName;
	
	private Charset consoleCharset;
	
	@Autowired
	private KeyValueDbService keyValueDbService;

	@Autowired
	private KeyValueService keyValueService;

	@PostConstruct
	public void post() throws IOException, AppConfigurationError {
		setupSsh();
		this.psdataDirPath = Paths.get(this.psdataDir);
		if (!Files.exists(this.psdataDirPath)) {
			Files.createDirectories(this.psdataDirPath);
		}
		this.psappPath = Paths.get(this.psapp);
		
		if (!Files.exists(this.psappPath)) {
			throw new AppConfigurationError("psapp", this.psapp);
		}
		this.consoleCharset = Charset.forName(this.consoleCharsetName);
	}

	private void setupSsh() {
		KeyValueProperties sshKvp = keyValueService.getPropertiesByPrefix(MYAPP_PREFIX, "ssh");

		if (!sshKvp.containsKey(SshConfig.SSH_ID_RSA_KEY)) {
			KeyValue kv = new KeyValue(new String[] { MYAPP_PREFIX, "ssh", SshConfig.SSH_ID_RSA_KEY },
					getSsh().getSshIdrsa());
			keyValueDbService.save(kv);
		} else {
			getSsh().setSshIdrsa(sshKvp.getProperty(SshConfig.SSH_ID_RSA_KEY));
		}

		if (!sshKvp.containsKey(SshConfig.KNOWN_HOSTS_KEY)) {
			KeyValue kv = new KeyValue(new String[] { MYAPP_PREFIX, "ssh", SshConfig.KNOWN_HOSTS_KEY },
					getSsh().getKnownHosts());
			keyValueDbService.save(kv);
		} else {
			getSsh().setKnownHosts(sshKvp.getProperty(SshConfig.KNOWN_HOSTS_KEY));
		}
	}


	public SshConfig getSsh() {
		return ssh;
	}

	public void setSsh(SshConfig ssh) {
		this.ssh = ssh;
	}

	public Set<String> getStorageExcludes() {
		return storageExcludes;
	}

	public void setStorageExcludes(Set<String> storageExcludes) {
		this.storageExcludes = storageExcludes;
	}

	public CacheTimes getCache() {
		return cache;
	}

	public void setCache(CacheTimes cache) {
		this.cache = cache;
	}


	public String getPsdataDir() {
		return psdataDir;
	}

	public void setPsdataDir(String psdataDir) {
		this.psdataDir = psdataDir;
	}

	public Path getPsdataDirPath() {
		return psdataDirPath;
	}

	public void setPsdataDirPath(Path psdataDirPath) {
		this.psdataDirPath = psdataDirPath;
	}

	public String getPsapp() {
		return psapp;
	}

	public void setPsapp(String psapp) {
		this.psapp = psapp;
	}

	public String getPowershell() {
		return powershell;
	}

	public void setPowershell(String powershell) {
		this.powershell = powershell;
	}

	public Path getPsappPath() {
		return psappPath;
	}

	public void setPsappPath(Path psappPath) {
		this.psappPath = psappPath;
	}

	public String getConsoleCharsetName() {
		return consoleCharsetName;
	}

	public void setConsoleCharsetName(String consoleCharsetName) {
		this.consoleCharsetName = consoleCharsetName;
	}

	public Charset getConsoleCharset() {
		return consoleCharset;
	}

	public void setConsoleCharset(Charset consoleCharset) {
		this.consoleCharset = consoleCharset;
	}

	public static class CacheTimes {

		private int combo;

		public int getCombo() {
			return combo;
		}

		public void setCombo(int combo) {
			this.combo = combo;
		}
	}

	public static class SshConfig implements Serializable {

		public static final String SSH_ID_RSA_KEY = "sshIdrsa";
		public static final String KNOWN_HOSTS_KEY = "knownHosts";
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@NotEmpty
		private String sshIdrsa;
		@NotEmpty
		private String knownHosts;

		public SshConfig() {
		}

		public SshConfig(KeyValueProperties kvp) {
			setSshIdrsa(kvp.getProperty(SSH_ID_RSA_KEY));
			setKnownHosts(kvp.getProperty(KNOWN_HOSTS_KEY));
		}

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

		public boolean knownHostsExists() {
			return knownHosts != null && !knownHosts.trim().isEmpty() && Files.exists(Paths.get(knownHosts));
		}

		public boolean sshIdrsaExists() {
			return sshIdrsa != null && !sshIdrsa.trim().isEmpty() && Files.exists(Paths.get(sshIdrsa.trim()));
		}
	}
}
