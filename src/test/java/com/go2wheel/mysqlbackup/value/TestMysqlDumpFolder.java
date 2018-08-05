package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;

public class TestMysqlDumpFolder {
	
	private Path demoFolder = Paths.get("fixtures","mysql", "dumpfolder");
	
	@Test
	public void t() throws IOException, UnExpectedInputException {
		MysqlDumpFolder md = new MysqlDumpFolder(demoFolder);
		assertThat(md.getLogFiles(), equalTo(2));
		assertThat(md.getDumpSize(), equalTo(56L));
		assertThat(md.getLogFileSize(), equalTo(168 + 120L));
		assertThat(md.getId(), equalTo("dumpfolder"));
	}

}
