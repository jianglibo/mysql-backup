package com.go2wheel.mysqlbackup.jsch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.jcraft.jsch.JSchException;

public class TestScpTo extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();	
    
    private Random random = new Random();
	
	@Before
	public void before() throws JSchException {
		clearDb();
		createSession();
	}

	@Test
	public void scpToFileToFile() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		Path tmpFile = tfolder.newFile().toPath();
		Files.write(tmpFile, "abc".getBytes());
		String rfile = "/tmp/" + tmpFile.getFileName().toString();
		String lfile = tmpFile.toAbsolutePath().toString();

		ScpUtil.to(session, lfile, rfile);
		
		List<String> er = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lh %s", rfile)).getAllTrimedNotEmptyLines();
		assertThat(er.size(), equalTo(1));
		SSHcommonUtil.deleteRemoteFile(session, rfile);
	}
	
	@Test
	public void scpToFileToDir() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		Path tmpFile = tfolder.newFile().toPath();
		Files.write(tmpFile, "abc".getBytes());
		String rfile = "/tmp";
		String lfile = tmpFile.toAbsolutePath().toString();

		ScpUtil.to(session, lfile, rfile);
		
		String rfullpath = "/tmp/" + tmpFile.getFileName().toString(); 
		
		List<String> er = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lh %s", rfullpath)).getAllTrimedNotEmptyLines();
		assertThat(er.size(), equalTo(1));
		SSHcommonUtil.deleteRemoteFile(session, rfullpath);
	}
	
	@Test
	public void scpToStringToFile() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		String rfile = "/tmp/" + random.nextDouble();
		ScpUtil.to(session, rfile, "abc".getBytes());
		List<String> er = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lh %s", rfile)).getAllTrimedNotEmptyLines();
		assertThat(er.size(), equalTo(1));
		
		assertThat(new String(ScpUtil.from(session, rfile).toByteArray()), equalTo("abc"));
		SSHcommonUtil.deleteRemoteFile(session, rfile);
	}
	
	@Test
	public void scpToStringToFile1() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		List<String> ss = new Random().ints(30, 200).limit(10).mapToObj(i -> {
			return new Random().ints(33, 126).limit(i).mapToObj(j -> (char)j + "").collect(Collectors.joining());
		}).collect(Collectors.toList());
		
		String rfile = "/tmp/random_content.txt";
		ScpUtil.to(session, rfile, Strings.join(ss, '\n').getBytes());
		List<String> er = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lh %s", rfile)).getAllTrimedNotEmptyLines();
		assertThat(er.size(), equalTo(1));

	}


}
