package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.util.FileUtil;

public class TestGetLogbinfiles {
	
	private Path fixture = Paths.get("fixtures", "mysql", "dumpfolder");
	
	private String bin1 = "hm-log-bin.000329";
	private String bin2 = "hm-log-bin.000330";
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();	
    
    @Test
    public void tSuccess() throws IOException, UnExpectedInputException {
    	FileUtil.copyDirectory(fixture, tfolder.getRoot().toPath(), false);
    	
    	MysqlService ms = new MysqlService();
    	
    	List<Path> files = ms.getLogBinFiles(tfolder.getRoot().toPath());
    	assertThat(files.size(), equalTo(2));
    	assertThat(files.get(0).getFileName().toString(), equalTo(bin1));
    	assertThat(files.get(1).getFileName().toString(), equalTo(bin2));
    }
    
    @Test
    public void tWrongFormat() throws IOException, UnExpectedInputException {
    	FileUtil.copyDirectory(fixture, tfolder.getRoot().toPath(), false);
    	
    	Files.delete(tfolder.getRoot().toPath().resolve(bin1));
    	Files.delete(tfolder.getRoot().toPath().resolve(bin2));
    	MysqlService ms = new MysqlService();
    	try {
			ms.getLogBinFiles(tfolder.getRoot().toPath());
		} catch (Exception e) {
			assertTrue(e instanceof UnExpectedInputException);
			UnExpectedInputException ue = (UnExpectedInputException) e;
			assertThat(ue.getMsgkey(), equalTo("dumpfolder.wrongformat"));
		}
    	
    }


}
