package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.job.SchedulerService;


public class TestConfigFileLoader extends SpringBaseFort {
	
	
	@Value("${fort.borg.configuration.file}")
	private String borgconfigfile;
	
	@Autowired
	private ConfigFileLoader configFileLoader;
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
    @Autowired
    private SchedulerService schedulerService;
    
    /**
     * all files in target directory which having json extension should be a valid configuration file.
     * @throws IOException
     * @throws ExecutionException
     */
    @Test(expected=ExecutionException.class)
    public void tLoadAllThrowException() throws IOException, ExecutionException {
    	Path root = repofolder.getRoot().toPath();
    	Path bcf = Paths.get(borgconfigfile);
    	Files.copy(bcf, root.resolve(bcf.getFileName()));
		File f = repofolder.newFile("a.json");
		Files.write(f.toPath(), "abc".getBytes());
		configFileLoader.loadAll(root);
		configFileLoader.clearCache();
    }
    
    @Test
    public void tLoadAll() throws IOException, ExecutionException {
    	Path root = repofolder.getRoot().toPath();
    	Path bcf = Paths.get(borgconfigfile);
    	Files.copy(bcf, root.resolve(bcf.getFileName()));
		configFileLoader.loadAll(root);
		configFileLoader.clearCache();
    }
    
    @Test
    public void tScheduleAll() throws IOException, ExecutionException, SchedulerException, ParseException {
    	Path root = repofolder.getRoot().toPath();
    	Path bcf = Paths.get(borgconfigfile);
    	Files.copy(bcf, root.resolve(bcf.getFileName()));
		configFileLoader.loadAll(root);
		List<Trigger> triggers = schedulerService.getAllTriggers();
		assertThat(triggers.size(), equalTo(0));
		configFileLoader.scheduleAll();
		
		triggers = schedulerService.getAllTriggers();
		assertThat(triggers.size(), equalTo(2));
		configFileLoader.clearCache();
    }

}
