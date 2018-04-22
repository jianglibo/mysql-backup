package com.go2wheel.mysqlbackup.sshj;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import net.schmizz.sshj.xfer.FileSystemFile;

public class TestScpDownload extends SshBaseFort {
	
	@Test
	public void toDstFile() throws IOException {
		createAfileOnServer();
		Path p = Files.createTempFile("scp", "txt");
		FileSystemFile sfs = new FileSystemFile(p.toFile());
        sshClient.newSCPFileTransfer().download(TMP_SERVER_FILE_NAME, sfs);
        List<String> lines = Files.readAllLines(sfs.getFile().toPath());
        assertThat(lines.size(), equalTo(1));
        assertThat(lines.get(0), equalTo("abc"));
        
        lines = Files.readAllLines(p);
        assertThat(lines.size(), equalTo(1));
        assertThat(lines.get(0), equalTo("abc"));
	}
	
	@Test
	public void toDstFolder() throws IOException {
		createAfileOnServer();
		Path p = Files.createTempDirectory("scpdir");
		FileSystemFile sfs = new FileSystemFile(p.toFile());
        sshClient.newSCPFileTransfer().download(TMP_SERVER_FILE_NAME, sfs);
        List<Path> paths = Files.list(p).collect(Collectors.toList());
        assertThat(paths.size(), equalTo(1));
        List<String> lines = Files.readAllLines(p.resolve(paths.get(0)));
        assertThat(lines.size(), equalTo(1));
        assertThat(lines.get(0), equalTo("abc"));
	}
	
	@Test
	public void fromFolderToDstFolder() throws IOException {
		createADirOnServer(2);
		Path p = Files.createTempDirectory("scpdir");
		FileSystemFile sfs = new FileSystemFile(p.toFile());
        sshClient.newSCPFileTransfer().download(TMP_SERVER_DIR_NAME, sfs); // scp /tmp/abc localfolder
        assertDirectory(p, 1, 0, 6);
        
	}
	
	@Test
	public void fromFolderToDstFolder1() throws IOException {
		createADirOnServer(2);
		Path p = Files.createTempDirectory("scpdir");
		FileSystemFile sfs = new FileSystemFile(p.toFile());
        sshClient.newSCPFileTransfer().download(TMP_SERVER_DIR_NAME + "/*", sfs); // scp /tmp/abc/* localfolder 
        assertDirectory(p, 1, 2, 5);
	}

}
