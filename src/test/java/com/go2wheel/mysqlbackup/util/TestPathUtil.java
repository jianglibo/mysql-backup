package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;

public class TestPathUtil {
	
	private Path dir;
	
	@Before
	public void before() throws IOException {
		dir = Files.createTempDirectory("pathutil");
	}
	
	@After
	public void after() throws IOException {
		FileUtil.deleteFolder(dir);
	}

	@Test
	public void t() {
		String s = PathUtil.replaceDotWithSlash(".");
		assertThat(s, equalTo("/"));

		s = PathUtil.replaceDotWithSlash("a");
		assertThat(s, equalTo("a"));

		s = PathUtil.replaceDotWithSlash("a.");
		assertThat(s, equalTo("a/"));
		
		s = PathUtil.replaceDotWithSlash("..");
		assertThat(s, equalTo("//"));
	}
	
	@Test
	public void testJarLocation() throws URISyntaxException {
		Optional<Path> p = PathUtil.getJarLocation();
		assertTrue(p.isPresent());
	}
	
	
	@Test
	public void tNextAvailable() {
		Path p = PathUtil.getNextAvailable(dir, "a.b", 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.0"));
	}
	
	@Test
	public void tNextAvailable1() throws IOException {
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(dir, "a.b", 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.1"));
	}
	
	@Test
	public void tNextAvailable11() throws IOException {
		Files.write(dir.resolve("a.b.0"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(dir.resolve("a.b"), 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.1"));
	}
	
	@Test
	public void tNextAvailable2() throws IOException {
		Files.write(dir.resolve("a.b.00"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(dir, "a.b", 2);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.01"));
	}
	
	@Test
	public void tNextAvailable22() throws IOException {
		Files.write(dir.resolve("a.b.00"), "abc".getBytes());
		Files.write(dir.resolve("a.b.01"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(dir, "a.b", 2);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.02"));
	}


}
