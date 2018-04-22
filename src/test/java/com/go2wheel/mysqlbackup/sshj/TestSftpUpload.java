package com.go2wheel.mysqlbackup.sshj;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class TestSftpUpload extends SshBaseFort {
	
	@Test
	public void tUploadFile() throws IOException {
            Path lf = createALocalFile();
            final SFTPClient sftp = sshClient.newSFTPClient();
            try {
                sftp.put(new FileSystemFile(lf.toFile()), "/tmp");
            } finally {
                sftp.close();
            }

	}

}
