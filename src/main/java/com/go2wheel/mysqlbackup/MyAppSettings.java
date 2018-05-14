package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.MysqlDumpException;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;

@ConfigurationProperties(prefix = "myapp")
@Component
public class MyAppSettings {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private SshConfig ssh;
	
	private String dataDir;
	
	private Path dataRoot;
	
	private String downloadFolder;
	
	private Path downloadRoot;
	
	@PostConstruct
	public void post() throws IOException {
		if (!StringUtil.hasAnyNonBlankWord(dataDir)) {
			this.dataDir = "boxes";
		}
		Path tmp = Paths.get(this.dataDir);
		if (!tmp.isAbsolute()) {
			tmp = PathUtil.getJarLocation().get().resolve(this.dataDir);
		}
		if (!Files.exists(tmp)) {
			Files.createDirectories(tmp);
		}
		this.dataRoot = tmp;
		
		logger.info("dataRoot: {}", this.dataRoot);
		
		tmp = Paths.get(this.downloadFolder);
		if (!tmp.isAbsolute()) {
			tmp = PathUtil.getJarLocation().get().resolve(this.downloadFolder);
		}
		if (!Files.exists(tmp)) {
			Files.createDirectories(tmp);
		}
		this.downloadRoot = tmp;
		
		logger.info("downloadRoot: {}", this.downloadRoot);

	}
	
	private Path getHostDir(Box box) {
		return getDataRoot().resolve(box.getHost());
	}

	public Path getLogBinDir(Box box) {
		Path dstDir = getHostDir(box).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			try {
				Files.createDirectories(dstDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dstDir;
	}
	
	public Path getBorgRepoDir(Box box) {
		Path dstDir = getHostDir(box).resolve("repo");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			try {
				Files.createDirectories(dstDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dstDir;
	}

	
	public Path getDumpDir(Box box) {
		try {
			Path dstDir = getHostDir(box).resolve("dump");
			if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
				Files.createDirectories(dstDir);
			}
			return dstDir;
		} catch (IOException e) {
			throw new MysqlDumpException(box, "create dump folder failed.");
		}
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
		
		public boolean knownHostsExists() {
			return knownHosts != null && !knownHosts.trim().isEmpty() && Files.exists(Paths.get(knownHosts));
		}
		
		public boolean sshIdrsaExists() {
			return sshIdrsa != null && !sshIdrsa.trim().isEmpty() && Files.exists(Paths.get(sshIdrsa));
		}
	}
}
