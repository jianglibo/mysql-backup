package com.go2wheel.mysqlbackup.robocopy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.ProcessExecUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

public class TestRoboCopy extends SpringBaseFort {
	
	private Path src = Paths.get("e:", "workspace-sts-2.9.1.RELEASE");
	
	private Path dst = Paths.get("e:", "workspace-sts-2.9.1.RELEASE.1");
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();	
	
	@Autowired
	private ProcessExecUtil peu;
	
	@Test
	public void t() throws IOException, InterruptedException {
		assumeTrue(Files.exists(src));
		ProcessExecResult per = peu.runDos("dir", src.toAbsolutePath().toString());
		assertThat(per.getExitValue(), equalTo(0));
		assertTrue(per.getStdError().size() == 0);
		assertTrue(per.getStdOut().size() > 0);
	}
	
	@Test
	public void tMir() throws IOException, InterruptedException {
		assumeTrue(Files.exists(src));
		ProcessExecResult per = peu.run(Arrays.asList("robocopy.exe", src.toString(), dst.toString(), "/MIR"));
		assertTrue((!per.getTimeCost(TimeUnit.SECONDS).isEmpty()));
		assertThat(per.getExitValue(), equalTo(0));
		assertTrue(per.getStdError().size() == 0);
		assertTrue(per.getStdOut().size() > 0);
	}
	
	@Test
	public void troboMirror() throws IOException, InterruptedException {
		assumeTrue(Files.exists(src));
		Path tmpFile = Files.createTempFile("robo", "log");
		ProcessExecResult per = peu.roboCopy(src.toString(), dst.toString(), tmpFile);
		assertTrue((!per.getTimeCost(TimeUnit.SECONDS).isEmpty()));
		assertThat(per.getExitValue(), equalTo(0));
		assertTrue(per.getStdError().size() == 0);
		assertTrue(per.getStdOut().size() == 0);
	}

}
