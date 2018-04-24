package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;

public class TestSSHcommonUtil extends SshBaseFort {
	
	
	@Test
	public void tWriteRemoteFile() throws IOException {
		String rfn = "/tmp/hello.txt";
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		SSHcommonUtil.writeRemoteFile(sshClient, rfn, "abc");
		
		String content = SSHcommonUtil.getRemoteFileContent(sshClient, rfn);
		assertThat(content, equalTo("abc"));
	}

}
