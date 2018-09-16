package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.go2wheel.mysqlbackup.RemoteTfolder;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.FileToCopyInfo;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestSSHcommonUtil extends SpringBaseFort {
	
	private String rf = "/t/t";
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    @Rule
    public RemoteTfolder rtfoler = new RemoteTfolder(rf);
	
	@Before
	public void before() throws JSchException {
		clearDb();
		createSession();
	}
	
	@Test
	public void tAllFileExists() throws IOException, ScpException, JSchException {
		rtfoler.setSession(session);
		boolean b = SSHcommonUtil.allFileExists(session, "/etc", "/var1", "/var");
		assertFalse(b);
		
		b = SSHcommonUtil.allFileExists(session, "/etc", "/var");
		assertTrue(b);

	}
	
	@Test
	public void tWriteRemoteFile() throws IOException, ScpException, JSchException {
		rtfoler.setSession(session);
		String rfn = rtfoler.newFile("hello.txt");
		SSHcommonUtil.copy(server.getOs(), session, rfn, "abc".getBytes());
		
		String content = ScpUtil.from(session, rfn).toString();
		assertThat(content, equalTo("abc"));
	}

	@Test
	public void tBackupByMove() throws IOException, ScpException, JSchException, RunRemoteCommandException {
		rtfoler.setSession(session);
		String rf = rtfoler.newFile("abc");
		
		createADirOnServer(rf, "abc", 3);
		
		SSHcommonUtil.backupFileByMove(session, rf);
		assertFalse(SSHcommonUtil.fileExists(server.getOs(), session, rf));
		assertTrue(SSHcommonUtil.fileExists(server.getOs(), session, rf + ".1"));
	}
	
	@Test
	public void testUploadFolder() throws IOException, RunRemoteCommandException, JSchException {
		rtfoler.setSession(session);
		
		File fo = tfolder.newFolder("fo");
		File foo = new File(fo, "foo");
		foo.mkdirs();
		File f1 = new File(fo, "a1.txt");
		File f2 = new File(foo, "a1.txt");
		Files.write(f1.toPath(), "abc".getBytes());
		Files.write(f2.toPath(), "abc".getBytes());
		List<FileToCopyInfo> copyInfos = SSHcommonUtil.copyFolder(server.getOs(), session, tfolder.getRoot().toPath(), rtfoler.getRemoteFolder());
		// include root item.
		assertThat(copyInfos.size(), equalTo(5));
		List<LinuxLsl> lsl = SSHcommonUtil.listRemoteFilesRecursive(session, rtfoler.getRemoteFolder());
		// fo, foo, f1, f2 == 4, include /t/t self.
		assertThat(lsl.size(), equalTo(5));
		String n1 = lsl.stream().filter(ls -> ls.getFilename().contains("/fo/foo/a1.txt")).findAny().get().getFilename();
		assertThat(n1, equalTo(rtfoler.newFile("fo/foo/a1.txt")));
	}
	
	@Test
	public void tbackupFileExist() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		rtfoler.setSession(session);
		String rfn = rtfoler.newFile("hello.txt");
		
		SSHcommonUtil.copy(server.getOs(), session, rfn, "abc".getBytes());
		
		SSHcommonUtil.backupFile(session, server, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(2));
		assertThat(fns.get(0), equalTo(rfn));
		assertThat(fns.get(1), equalTo(rfn + ".1"));
		
		
		SSHcommonUtil.revertFile(server.getOs(), session, rfn);
		fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(1));
		assertThat(fns.get(0), equalTo(rfn));
	}
	
	
	@Test
	public void tbackupNotFileExist() throws IOException, JSchException, RunRemoteCommandException {
		rtfoler.setSession(session);
		String rfn = rtfoler.newFile("hello.txt");
		SSHcommonUtil.backupFile(session,server, rfn);
		List<String> fns = SSHcommonUtil.runRemoteCommand(session, String.format("ls %s", rfn + "*")).getAllTrimedNotEmptyLines();
		Collections.sort(fns);
		assertThat(fns.size(), equalTo(1)); // err output.
	}
	
	@Test
	public void testFileExists() throws RunRemoteCommandException, JSchException, IOException {
		boolean b1 = SSHcommonUtil.fileExists(server.getOs(), session, "/usr/bin");
		assertTrue(b1);
		boolean b2 = SSHcommonUtil.fileExists(server.getOs(), session, "/usr/bin11");
		assertFalse(b2);
	}
	
	@Test
	public void isExecutable() throws RunRemoteCommandException, JSchException, IOException {
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "kkzx");
		assertThat(rcr.getExitValue(), greaterThan(0));
	}
	
	@Test
	public void testCoreNumber() throws JSchException, IOException {
		int cores = SSHcommonUtil.coreNumber(session);
		assertThat(cores, greaterThan(0));
	}

}
