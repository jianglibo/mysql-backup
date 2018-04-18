package com.go2wheel.mysqlbackup.sshj;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class TestSftpUpload {
	
	@Test
	public void t() throws IOException {
        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("localhost");
        try {
            ssh.authPublickey(System.getProperty("user.name"));
            final String src = System.getProperty("user.home") + File.separator + "test_file";
            final SFTPClient sftp = ssh.newSFTPClient();
            try {
                sftp.put(new FileSystemFile(src), "/tmp");
            } finally {
                sftp.close();
            }
        } finally {
            ssh.disconnect();
        }
	}

}
