package com.go2wheel.mysqlbackup.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.NoActionException;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

public class TestBorgService extends SpringBaseFort {
	
	@Value("${fort.borg.configuration.file}")
	private String borgconfigfile;
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
	
	@Test
	public void texists() {
		boolean b = Files.exists(Paths.get(borgconfigfile));
		assertTrue(b);
	}
	
	@Test
	public void trun() throws ExecutionException, NoActionException, IOException {
		ProcessExecResult per = configFileLoader.runCommand(borgconfigfile, "archive");
		printProcessExecutionResult(per);
	}

}
