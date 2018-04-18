package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;

import org.junit.Test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class TestScpDownload {
	
	@Test
	public void t() throws IOException {
        SSHClient ssh = new SSHClient();
        // ssh.useCompression(); // Can lead to significant speedup (needs JZlib in classpath)
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));
            ssh.newSCPFileTransfer().download("test_file", new FileSystemFile("/tmp/"));
        } finally {
            ssh.disconnect();
        }
	}

}
