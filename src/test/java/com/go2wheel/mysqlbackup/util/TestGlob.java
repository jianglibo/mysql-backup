package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;

public class TestGlob {
	
	
	@Test
	public void tFolder() {
		Path p = Paths.get("bin");
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:/bin/");
		assertFalse("/bin/ should not matches bin.", matcher.matches(p));
		
		matcher =
			    FileSystems.getDefault().getPathMatcher("glob:bin/");
		assertFalse("bin/ should not matches bin", matcher.matches(p));
		
		matcher =
			    FileSystems.getDefault().getPathMatcher("glob:bin");
		assertTrue("bin should matches bin", matcher.matches(p));

	}
	
	
	@Test
	public void tSlashStarted() throws URISyntaxException, IOException {
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:/**/*.{java,class}");
		
		File f = JarLocationGetter.myLocation();
		Path bp = f.toPath();
		long c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
//			Path rp = bp.resolve(p);
			return matcher.matches(p);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("if / started then no file matches.", c, equalTo(0L));
	}
	
	@Test
	public void tMatchAll() throws URISyntaxException, IOException {
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:**/*");
		
		File f = JarLocationGetter.myLocation();
		Path bp = f.toPath();
		long c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
			Path rp = bp.resolve(p);
			return matcher.matches(rp);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("**/* should match all.", c, greaterThan(10L));
	}
	
	@Test
	public void tBackSlash() throws URISyntaxException, IOException {
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:**\\\\*.{java,class}");
		
		File f = JarLocationGetter.myLocation();
		Path bp = f.toPath();
		long c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
			Path rp = bp.resolve(p);
			return matcher.matches(rp);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("4 blackslash should path separator.", c, greaterThan(10L));
	}
	
	
	@Test
	public void tSlashStyle() throws URISyntaxException, IOException {
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:**/*.{java,class}");
		
		File f = JarLocationGetter.myLocation();
		Path bp = f.toPath();
		long c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
			Path rp = bp.resolve(p);
			return matcher.matches(rp);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("**/ matchs all folder beneath.", c, greaterThan(10L));
		
		
		PathMatcher matcher1 =
			    FileSystems.getDefault().getPathMatcher("glob:**\\*.{java,class}");
		
		c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
			Path rp = bp.resolve(p);
			return matcher1.matches(rp);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("**\\ matchs no folder.", c, equalTo(0L));
		
	
		PathMatcher matcher3 =
			    FileSystems.getDefault().getPathMatcher("glob:**/*");
		
		c = Files.find(bp, Integer.MAX_VALUE, (p, a) -> {
			Path rp = bp.resolve(p);
			return matcher3.matches(rp);
		}).map(p -> bp.resolve(p)).count();
		
		assertThat("**/ matchs all folder beneath.", c, greaterThan(10L));
	}
	
	@Test
	public void tGitignore() {
		Path gitignore = UtilForTe.getPathInThisProjectRelative(".gitignore");
		try (Stream<String> lines = Files.lines(gitignore)) {
			lines.filter(line -> !line.trim().startsWith("#")).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
