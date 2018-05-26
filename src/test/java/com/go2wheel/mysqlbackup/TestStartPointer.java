package com.go2wheel.mysqlbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.util.FileUtil;

public class TestStartPointer {
	
	private Path tmpFolder;
	
	private Path zipFile;
	
	@Before
	public void b() throws IOException {
		Path p = Paths.get("build", "dist");
		zipFile = Files.list(p).filter(pp -> pp.toString().endsWith(".zip")).findAny().get();
		tmpFolder = Files.createTempDirectory("test");
	}
	
	@After
	public void a() throws IOException {
		FileUtil.deleteFolder(tmpFolder);
	}
	
	@Test
	public void t() throws IOException {
		StartPointer.doUpgrade(null, new String[] {"--spring.datasource.url=jdbc:hsqldb:file: ;shutdown=true"});
	}

}
