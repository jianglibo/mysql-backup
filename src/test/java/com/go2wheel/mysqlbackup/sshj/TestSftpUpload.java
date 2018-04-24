package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
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

//	@Test
//	public void tWriteRemoteFile() throws IOException {
//		final String rfn = "/tmp/sftpopen.txt";
//		SSHcommonUtil.touchAfile(sshClient, rfn);
//        final SFTPClient sftp = sshClient.newSFTPClient();
//         
//        Set<OpenMode> om = new HashSet<>();
//        om.add(OpenMode.CREAT);
//        RemoteFile rf = sftp.open(rfn, om);
//        byte[] bytes = "abc".getBytes(); 
//        rf.write(0, bytes, 0, bytes.length);
//        rf.close();
//        String content = SSHcommonUtil.getRemoteFileContent(sshClient, rfn);
//        SSHcommonUtil.deleteRemoteFile(sshClient, rfn);
//        assertThat(content, equalTo("abc"));
//	}

}
