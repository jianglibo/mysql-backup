package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;

@Component
public class SshClientFactory {
	
	private MyAppSettings appSettings;

	public SSHClient getConnectedSSHClient(MysqlInstance instance) throws IOException {
		final SSHClient ssh = new SSHClient();
		Path knownHosts = Paths.get(appSettings.getSsh().getKnownHosts());
		ssh.loadKnownHosts(knownHosts.toFile());
		ssh.connect(instance.getHost(), instance.getSshPort() == 0 ? 22 : instance.getSshPort());
		ssh.authPublickey(instance.getUsername(), appSettings.getSsh().getSshIdrsa());
		return ssh;
	}
	
	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
}
