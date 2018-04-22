package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;

@Component
public class SshClientFactory {

	private MyAppSettings appSettings;

	private Logger logger = LoggerFactory.getLogger(SshClientFactory.class);

	public Optional<SSHClient> getConnectedSSHClient(MysqlInstance instance) throws IOException {
		final SSHClient ssh = new SSHClient();

		if (instance.hasFingerPrint()) {
			ssh.addHostKeyVerifier(instance.getFingerprint());
		} else if(appSettings.getSsh().knownHostsExists()) {
			Path knownHosts = Paths.get(appSettings.getSsh().getKnownHosts());
			ssh.loadKnownHosts(knownHosts.toFile());
		} else {
			logger.error("instance: {}, message: {}", instance, "No way to verify it's a known host.");
			try {
				ssh.close();
			} catch (Exception e1) {
			}
			return Optional.empty();
		}

		try {
			ssh.connect(instance.getHost(), instance.getSshPort() == 0 ? 22 : instance.getSshPort());
		} catch (Exception e2) {
			logger.error("instance: {}, message: {}", instance, e2.getMessage());
			try {
				ssh.close();
			} catch (Exception e1) {
			}
			return Optional.empty();
		}
		
		try {
			if (instance.canSShKeyAuth()) {
				ssh.authPublickey(instance.getUsername(), instance.getSshKeyFile());
			} else if (instance.canPasswordAuth()) {
				ssh.authPassword(instance.getUsername(), instance.getPassword());
			} else if(appSettings.getSsh().sshIdrsaExists()) {
				ssh.authPublickey(instance.getUsername(), appSettings.getSsh().getSshIdrsa());
			}
		} catch (Exception e) {
			logger.error("instance: {}, message: {}", instance, e.getMessage());
			try {
				ssh.close();
			} catch (Exception e1) {
			}
			return Optional.empty();
		}

		return Optional.of(ssh);
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
}
