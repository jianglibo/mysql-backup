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
import com.go2wheel.mysqlbackup.value.Box;

import net.schmizz.sshj.SSHClient;

@Component
public class SshClientFactory {

	private MyAppSettings appSettings;

	private Logger logger = LoggerFactory.getLogger(SshClientFactory.class);

	public Optional<SSHClient> getConnectedSSHClient(Box box) {
		final SSHClient ssh = new SSHClient();

		if (box.hasFingerPrint()) {
			ssh.addHostKeyVerifier(box.getFingerprint());
		} else if(appSettings.getSsh().knownHostsExists()) {
			Path knownHosts = Paths.get(appSettings.getSsh().getKnownHosts());
			try {
				ssh.loadKnownHosts(knownHosts.toFile());
			} catch (IOException e) {
				logger.error("knownhosts configarated to: {}, but cannot be loaded.", appSettings.getSsh().getKnownHosts());
			}
		} else {
			logger.error("instance: {}, message: {}", box, "No way to verify it's a known host.");
			try {
				ssh.close();
			} catch (Exception e1) {
			}
			return Optional.empty();
		}

		try {
			ssh.connect(box.getHost(), box.getPort() == 0 ? 22 : box.getPort());
		} catch (Exception e2) {
			logger.error("instance: {}, message: {}", box, e2.getMessage());
			try {
				ssh.close();
			} catch (Exception e1) {
			}
			return Optional.empty();
		}
		
		try {
			if (box.canSShKeyAuth()) {
				ssh.authPublickey(box.getUsername(), box.getSshKeyFile());
			} else if (box.canPasswordAuth()) {
				ssh.authPassword(box.getUsername(), box.getPassword());
			} else if(appSettings.getSsh().sshIdrsaExists()) {
				ssh.authPublickey(box.getUsername(), appSettings.getSsh().getSshIdrsa());
			} else {
				logger.error("no authentication method found.");
			}
		} catch (Exception e) {
			logger.error("instance: {}, message: {}", box, e.getMessage());
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
