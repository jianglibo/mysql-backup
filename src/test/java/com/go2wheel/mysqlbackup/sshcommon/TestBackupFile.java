package com.go2wheel.mysqlbackup.sshcommon;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.RemoteTfolder;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.jcraft.jsch.JSchException;

public class TestBackupFile extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    public RemoteTfolder rtfolder = new RemoteTfolder("/tmp/abc");
	
	@Test
	public void tWin() throws SchedulerException, JSchException, IOException {
		createSessionLocalHostWindowsAfterClear();
		
		Path f = createALocalFile(tfolder.getRoot().toPath().resolve("ff"), "abc");
		assertTrue(Files.exists(f));
		SSHcommonUtil.backupFile(session,server, f.toAbsolutePath().toString());
		
		Path f1 = f.getParent().resolve("ff.1");
		
		assertTrue(Files.exists(f));
		assertTrue(Files.exists(f1));
		
		String c = new String(Files.readAllBytes(f1));
		assertThat(c, equalTo("abc"));
	}
	
	@Test
	public void tLinux() throws SchedulerException, JSchException, IOException {
		clearDb();
		createSession();
		deleteAllJobs();
		rtfolder.setSession(session);
		SSHcommonUtil.runRemoteCommand(session, "mkdir -p /tmp/abc");
		createAfileOnServer("/tmp/abc/aa", "abc");
		SSHcommonUtil.backupFile(session, server, "/tmp/abc/aa");
		boolean b = SSHcommonUtil.allFileExists(session, "/tmp/abc/aa", "/tmp/abc/aa.1");
		assertTrue(b);
	}

}
