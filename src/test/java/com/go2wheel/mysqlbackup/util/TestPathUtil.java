package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestPathUtil {
	
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    
    @Test
    public void tdouble() {
    	long l1 = 5;
    	long l2 = 6;
    	
    	double d = (double)l2/ l1;
    	
    	assertThat(d, greaterThan(1d));
    	
    	int i = (int) (((double)l2/ l1) * 100);
    	assertThat(i, equalTo(120));
    }
	
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
	
//	@Test
//	public void testIncreamentFileName() {
//		String f = PathUtil.increamentFileName("000");
//		assertThat(f, equalTo("000.0"));
//
//		f = PathUtil.increamentFileName("x.000");
//		assertThat(f, equalTo("x.001"));
//		
//		f = PathUtil.increamentFileName("x.009");
//		assertThat(f, equalTo("x.010"));
//	}
	
	
	@Test
	public void testReplaceFileName() {
		String origin = "c:\\a\\b.txt";
		String replaced = PathUtil.replaceFileName(origin, "1.txt");
		assertThat(replaced, equalTo("c:\\a\\1.txt"));
		
		origin = "/a/b.txt";
		replaced = PathUtil.replaceFileName(origin, "1.txt");
		assertThat(replaced, equalTo("/a/1.txt"));
		
		origin = "/a/b.txt/";
		replaced = PathUtil.replaceFileName(origin, "1.txt");
		assertThat(replaced, equalTo("/a/b.txt/1.txt"));


	}
	
	
	@Test
	public void testPathNameSort() throws IOException {
		Path root = tfolder.getRoot().toPath();
		tfolder.newFile("a");
		tfolder.newFile("aa");
		tfolder.newFile("aaa");
		
		List<Path> pathes = Files.list(root).collect(Collectors.toList());
		Collections.sort(pathes, PathUtil.PATH_NAME_ASC);
		
		assertThat(pathes.get(0).getFileName().toString(), equalTo("a"));
		assertThat(pathes.get(2).getFileName().toString(), equalTo("aaa"));
		
		Collections.sort(pathes, PathUtil.PATH_NAME_DESC);
		
		assertThat(pathes.get(0).getFileName().toString(), equalTo("aaa"));
		assertThat(pathes.get(2).getFileName().toString(), equalTo("a"));
	}
	
	@Test
	public void testPathNameSort1() throws IOException {
		Path root = tfolder.getRoot().toPath();
		tfolder.newFile("a.000");
		tfolder.newFile("a.001");
		tfolder.newFile("a.010");
		
		List<Path> pathes = Files.list(root).collect(Collectors.toList());
		Collections.sort(pathes, PathUtil.PATH_NAME_ASC);
		
		assertThat(pathes.get(0).getFileName().toString(), equalTo("a.000"));
		assertThat(pathes.get(2).getFileName().toString(), equalTo("a.010"));
		
		Collections.sort(pathes, PathUtil.PATH_NAME_DESC);
		
		assertThat(pathes.get(0).getFileName().toString(), equalTo("a.010"));
		assertThat(pathes.get(2).getFileName().toString(), equalTo("a.000"));
	}
	
	
	@Test
	public void getNextByBaseNameNotExists() throws IOException {
		Path fp = getRoot().resolve("a.b");
		Path np = PathUtil.getNextAvailableByBaseName(getRoot().resolve("a.b"), 1);
		assertThat(np.getFileName().toString(), equalTo("a.b.0"));
		
		Path max = PathUtil.getMaxVersionByBaseName(fp);
		assertThat(max.getFileName().toString(), equalTo("a.b"));
	}
	
	@Test
	public void getNextByBaseName() throws IOException {
		Path fp = getRoot().resolve("a.b");
		Files.write(fp, "abc".getBytes());
		Path np = PathUtil.getNextAvailableByBaseName(getRoot().resolve("a.b"), 1);
		assertThat(np.getFileName().toString(), equalTo("a.b.0"));
		
		Path max = PathUtil.getMaxVersionByBaseName(fp);
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
		
		Path max = PathUtil.getMaxVersionByBaseName(getRoot().resolve("a.b"));
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
	
	@Test
	public void testgetRidOfLastSlash() {
		String l = PathUtil.getRidOfLastSlash("abc///");
		assertThat(l, equalTo("abc"));
		
		l = PathUtil.getRidOfLastSlash("/");
		assertThat(l, equalTo(""));
	}
	
	
	@Test
	public void testJoin() {
		String s = PathUtil.join("a", "b", "c");
		assertThat(s, equalTo("a/b/c"));
		s = PathUtil.join("a", "b", "c/");
		assertThat(s, equalTo("a/b/c/"));
		
		s = PathUtil.join("a/");
		assertThat(s, equalTo("a/"));
	}

	@Test
	public void testGetFilename() {
		String s = PathUtil.getFileName("a/");
		assertThat(s, equalTo("a"));
		
		s = PathUtil.getFileName("/a");
		assertThat(s, equalTo("a"));
		
		s = PathUtil.getFileName("/");
		assertThat(s, equalTo(""));
		
		s = PathUtil.getFileName("c:\\a\\");
		assertThat(s, equalTo("a"));

	}
	
	@Test
	public void t() {
		String s = PathUtil.getParentWithEndingSeparator("/var/lig/abc");
		assertThat(s, equalTo("/var/lig/"));
		
		s = PathUtil.getParentWithEndingSeparator("/var");
		assertThat(s, equalTo("/"));
		
		s = PathUtil.getParentWithEndingSeparator("var");
		assertThat(s, equalTo(""));

		s = PathUtil.getParentWithEndingSeparator("c:\\var\\uv");
		assertThat(s, equalTo("c:\\var\\"));
		
	}

}
