package com.go2wheel.mysqlbackup.sshj;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class TestScpUpload {
	
	@Test
	public void t() throws IOException {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));

            // Present here to demo algorithm renegotiation - could have just put this before connect()
            // Make sure JZlib is in classpath for this to work
            ssh.useCompression();

            final String src = System.getProperty("user.home") + File.separator + "test_file";
            ssh.newSCPFileTransfer().upload(new FileSystemFile(src), "/tmp/");
        } finally {
            ssh.disconnect();
        }
	}

}
