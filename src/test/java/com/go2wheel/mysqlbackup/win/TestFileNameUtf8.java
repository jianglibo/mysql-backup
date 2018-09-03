package com.go2wheel.mysqlbackup.win;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestFileNameUtf8 {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    private Path fixture = Paths.get("fixtures", "chinesename");
	
	@Test
	public void tCreateAndConsumeByJava() throws IOException {
		Path cpath = tfolder.getRoot().toPath().resolve("中文.txt");
		Files.write(cpath, "abc".getBytes());
		
		Path tp = Files.list(tfolder.getRoot().toPath()).findAny().get();
		
		assertThat(tp.getFileName().toString(), equalTo("中文.txt"));
	}
	
	@Test
	public void tConsumeByJava() throws IOException {
		Path tp = Files.list(fixture).findAny().get();
		String fn = "中文目录";
		assertThat(tp.getFileName().toString(), equalTo(fn));
	}

}
