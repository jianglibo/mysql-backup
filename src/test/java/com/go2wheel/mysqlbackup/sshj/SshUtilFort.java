package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.go2wheel.mysqlbackup.YmlConfigFort;

import net.schmizz.sshj.SSHClient;

public class SshUtilFort {
	
	public static SSHClient getConnectedSSHClient(YmlConfigFort c) throws IOException {
		final SSHClient ssh = new SSHClient();
		Path knownHosts = Paths.get(c.getKnownHosts());
		ssh.loadKnownHosts(knownHosts.toFile());
		ssh.connect(c.getSshHost());
		ssh.authPublickey("root", c.getSshIdrsa());
		return ssh;
	}

}
