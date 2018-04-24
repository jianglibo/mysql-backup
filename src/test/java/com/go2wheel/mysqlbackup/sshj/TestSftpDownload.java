package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;

public class TestSftpDownload extends SshBaseFort {

//	@Test
//	public void tDownloadAfile() throws IOException {
//		createAfileOnServer();
//		Path dst = UtilForTe.createTmpDirectory();
//		final SFTPClient sftp = sshSession.newSFTPClient();
//		try {
//			sftp.get(TMP_SERVER_FILE_NAME, new FileSystemFile(dst.toFile()));
//		} finally {
//			sftp.close();
//		}
//		assertThat(new String(Files.readAllBytes(dst.resolve("abc.txt"))).trim(), equalTo(TMP_FILE_CONTENT));
//	}

}
