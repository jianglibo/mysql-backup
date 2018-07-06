package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.io.Serializable;
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

import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.KeyValueDbService;
import com.go2wheel.mysqlbackup.service.KeyValueService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;

@ConfigurationProperties(prefix = com.go2wheel.mysqlbackup.MyAppSettings.MYAPP_PREFIX)
@Component
public class MyAppSettings {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String MYAPP_PREFIX = "myapp";

	private SshConfig ssh;

	private String dataDir;

	private Path dataRoot;

	private String downloadFolder;

	private Path downloadRoot;

	private Set<String> storageExcludes;

	private CacheTimes cache;
	
	private KeyValueProperties kvp;

	// myapp.dataDir=boxes
	// myapp.downloadFolder=notingit
	// myapp.ssh.sshIdrsa=G:/cygwin64/home/Administrator/.ssh/id_rsa
	// myapp.ssh.knownHosts=G:/cygwin64/home/Administrator/.ssh/known_hosts
	// myapp.jp.maxThreads=3
	// myapp.jp.waitPause=10
	// myapp.jp.maxWait=10000
	// myapp.jp.remoteMode=false
	// myapp.storage_excludes[0]=/dev
	// myapp.storage_excludes[1]=/dev/shm
	// myapp.storage_excludes[2]=/run
	// myapp.storage_excludes[3]=/sys/fs/cgroup
	// myapp.storage_excludes[4]=/boot
	// myapp.storage_excludes[5]=/run/user/0
	// myapp.storage_excludes[6]=/run/user/1000
	// myapp.storage_excludes[7]=挂载点
	// myapp.cache.combo=0

	@Autowired
	private KeyValueDbService keyValueDbService;

	@Autowired
	private KeyValueService keyValueService;

	@PostConstruct
	public void post() throws IOException {
		setupProperties();
		setupSsh();
		setupDirectories();

	}

	private void setupProperties() {
		kvp = keyValueService.getPropertiesByPrefix(MYAPP_PREFIX);
	}

	private void setupDirectories() {
		try {
			if (!StringUtil.hasAnyNonBlankWord(dataDir)) {
				this.dataDir = "boxes";
			}
			Path tmp = Paths.get(this.dataDir);

			if (!Files.exists(tmp)) {
				Files.createDirectories(tmp);
			}
			this.dataRoot = tmp;

			logger.error("downloadFolder cofiguration value is: {}", this.downloadFolder);

			tmp = Paths.get(this.downloadFolder);

			if (!Files.exists(tmp)) {
				Files.createDirectories(tmp);
			}
			this.downloadRoot = tmp;
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}

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

	private Path getHostDir(Server server) {
		return getDataRoot().resolve(server.getHost());
	}

	public Path getLogBinDir(Server server) {
		Path dstDir = getHostDir(server).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			try {
				Files.createDirectories(dstDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dstDir;
	}

	public Path getBorgRepoDir(Server server) {
		Path dstDir = getHostDir(server).resolve("repo");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			try {
				Files.createDirectories(dstDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dstDir;
	}

	public Path getDumpDir(Server server) throws IOException {
		Path dstDir = getHostDir(server).resolve("dump");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		return dstDir;
	}

	public Path getLocalMysqlDir(Server server) throws IOException {
		Path dstDir = getHostDir(server).resolve("mysql");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		return dstDir;
	}

	public SshConfig getSsh() {
		return ssh;
	}

	public void setSsh(SshConfig ssh) {
		this.ssh = ssh;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public Path getDataRoot() {
		return dataRoot;
	}

	public void setDataRoot(Path dataRoot) {
		this.dataRoot = dataRoot;
	}

	public String getDownloadFolder() {
		return downloadFolder;
	}

	public void setDownloadFolder(String downloadFolder) {
		this.downloadFolder = downloadFolder;
	}

	public Path getDownloadRoot() {
		return downloadRoot;
	}

	public void setDownloadRoot(Path downloadRoot) {
		this.downloadRoot = downloadRoot;
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
