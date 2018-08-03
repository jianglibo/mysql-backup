package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestPathUtil {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
	
	@Test
	public void tReplaceDotWithSlash() {
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
	public void getNextByBaseName() throws IOException {
		Path fp = getRoot().resolve("a.b");
		Files.write(fp, "abc".getBytes());
		Path np = PathUtil.getNextAvailableByBaseName(getRoot().resolve("a.b"), 1);
		assertThat(np.getFileName().toString(), equalTo("a.b.0"));
		
		Path max = PathUtil.getMaxVersion(fp);
		assertThat(max.getFileName().toString(), equalTo("a.b"));
	}
	
	@Test
	public void testRoundNumber() throws IOException {
		Path fp = getRoot().resolve("a.b");
		Files.write(fp, "abc".getBytes());
		for(int i = 0; i< 9; i++) {
			fp = PathUtil.getNextAvailable(getRoot(), "a.b", 1, 4);
			Files.write(fp, "abc".getBytes());
		}
		// include a.b a.b.0 1 2 3
		assertThat(Files.list(getRoot()).count(), equalTo(5L));
		Path np = PathUtil.getNextAvailableByBaseName(getRoot().resolve("a.b"), 1);
		assertThat(np.getFileName().toString(), equalTo("a.b.4"));
		
		Path max = PathUtil.getMaxVersion(getRoot().resolve("a.b"));
		assertThat(max.getFileName().toString(), equalTo("a.b.3"));
		assertTrue(Files.exists(max));
	}
	
	@Test
	public void testJarLocation() throws URISyntaxException {
		Optional<Path> p = PathUtil.getJarLocation();
		assertTrue(p.isPresent());
	}
	
	
	@Test
	public void tNextAvailable() {
		Path p = PathUtil.getNextAvailable(getRoot(), "a.b", 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.0"));
	}
	
	@Test
	public void tNextAvailable1() throws IOException {
		Files.write(getRoot().resolve("a.b.0"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(getRoot(), "a.b", 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.1"));
	}
	
	@Test
	public void tNextAvailable11() throws IOException {
		Files.write(getRoot().resolve("a.b.0"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(getRoot().resolve("a.b"), 1);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.1"));
	}
	
	@Test
	public void tNextAvailable2() throws IOException {
		Files.write(getRoot().resolve("a.b.00"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(getRoot(), "a.b", 2);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.01"));
	}
	
	@Test
	public void tNextAvailable22() throws IOException {
		Files.write(getRoot().resolve("a.b.00"), "abc".getBytes());
		Files.write(getRoot().resolve("a.b.01"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(getRoot(), "a.b", 2);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.02"));
	}

	@Test
	public void tNextAvailableModoru() throws IOException {
		Files.write(getRoot().resolve("a.b.99"), "abc".getBytes());
		Path p = PathUtil.getNextAvailable(getRoot(), "a.b", 2);
		String fn = p.getFileName().toString();
		assertThat(fn, equalTo("a.b.00"));
	}
	
	private Path getRoot() {
		return tfolder.getRoot().toPath();
	}


}
