package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.value.CommonFileNames;

public class TestStaticUpgradeUtil {
	
	private Path unZippedPath;
	private Path curPath;
	private Path currentJar;
	private Path newJar;
	private Path dbDir;
	private Path templatesDir;
	private Path dbPath;
	private String newVersion = "1.111";
	
	
	private void createProperties(Path parent, String k, String v) throws IOException {
		Properties p = new Properties();
		p.put("a", "b");
		if (k != null) {
			p.put(k, v);
		}
		try (OutputStream os = Files.newOutputStream(parent.resolve(CommonFileNames.APPLICATION_CONFIGURATION))) {
			p.store(os, "");
			os.close();
		}
	}
	
	@Before
	public void b() throws IOException {
		unZippedPath = Files.createTempDirectory("test");
		curPath = Files.createTempDirectory("test");
		dbDir = Files.createDirectories(curPath.resolve("dbdata"));
		templatesDir = Files.createDirectories(curPath.resolve("templates"));
		dbPath = dbDir.resolve("db");
		Files.write(dbDir.resolve("db.tt"), "abc".getBytes());
		currentJar = curPath.resolve("aa-cc.jar");
		Files.write(currentJar, "bac".getBytes());
		
		newJar = unZippedPath.resolve("aa-cc-1.0.jar");
		Files.write(newJar, "HGellos".getBytes());
		
		Path unzipedTemplates = Files.createDirectories(unZippedPath.resolve("templates"));
		
		Files.write(unzipedTemplates.resolve("tpl.html"), "abc".getBytes());
		
		createProperties(curPath, null, null);
		createProperties(unZippedPath, "k", "v");
		
		Files.write(curPath.resolve(CommonFileNames.START_BATCH), "abc".getBytes());
		Files.write(unZippedPath.resolve(CommonFileNames.START_BATCH), "ccc".getBytes());
	}
	
	@After
	public void a() throws IOException {
		FileUtil.deleteFolder(unZippedPath);
		try {
			FileUtil.deleteFolder(curPath);
		} catch (Exception e) {
			long c = Files.list(curPath).count();
		}
	}
	
	@Test
	public void tDoChange() throws IOException {
		UpgradeUtil.doChange(curPath, unZippedPath, currentJar, newJar, dbDir, newVersion);
		assertTrue("new jar name should be aa-cc-1.0.jar", Files.exists(curPath.resolve("aa-cc-1.0.jar")));
		assertFalse("origin jar should removed to backup.", Files.exists(curPath.resolve("aa-cc.jar")));
		assertTrue("origin jar should rename to aa-cc.jar.001", Files.exists(curPath.resolve("aa-cc.jar.prev")));
		
		String bc = new String(Files.readAllBytes(curPath.resolve(CommonFileNames.START_BATCH)));
		assertThat("batch content should updated", bc, equalTo("ccc"));
		
		try (InputStream is = Files.newInputStream(curPath.resolve(CommonFileNames.APPLICATION_CONFIGURATION))) {
			Properties p = new Properties();
			p.load(is);
			assertThat("should contain k key with value v.", p.getProperty("k"), equalTo("v"));
		}
		
		
		assertTrue("dbPath should exist.", Files.exists(dbDir));
		
		assertTrue("dbPath should backuped..", Files.exists(dbDir.getParent().resolve(dbDir.getFileName().toString() + ".000")));
		
		
	}

}
