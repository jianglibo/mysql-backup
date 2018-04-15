package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;

public class TestGitignoreReader {
	
	private Path tmpFolder;
	
	
	@After
	public void after() throws IOException {
		if (tmpFolder != null) {
			UtilForTe.deleteFolder(tmpFolder);
		}
	}
	
	@Test
	public void oneMatcher() {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("bin"));
		assertThat("'bin' create one matcher.", pms.size(), equalTo(1));
		
		pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("/bin"));
		assertThat("'/bin' create one matcher.", pms.size(), equalTo(1));

	}
	
	@Test
	public void matcher3() {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("bin/"));
		assertThat("'bin/' create 3 matcher. 'bin', 'bin/', 'bin/**'", pms.size(), equalTo(3));
	}
	
	@Test
	public void matcher2() {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("**/bin"));
		assertThat("'bin/' create 3 matcher. 'bin', '**/bin'", pms.size(), equalTo(2));
	}
	
	@Test
	public void matcher6() {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("**/bin/"));
		assertThat("'bin/' create 3 matcher. **/bin, **/bin/, **/bin/**, bin, bin/, bin/**", pms.size(), equalTo(6));
	}

	
	@Test
	public void t() {
		Path ip = UtilForTe.getPathInThisProjectRelative("copyignore.txt");
		Path cp = UtilForTe.getPathInThisProjectRelative("bin");
		long c = GitIgnoreFileReader.ignoreMatchers(ip).stream().filter(pm -> {
				return pm.matches(cp);
			}
		).count();
		
		assertThat(c, equalTo(1L));
	}
	
	@Test
	public void t1() {
		Path ip = UtilForTe.getPathInThisProjectRelative("fixtures/folderignore.txt");
		int mc = GitIgnoreFileReader.ignoreMatchers(ip).size();
		
		assertThat(mc, equalTo(4));

		Path cp = UtilForTe.getPathInThisProjectRelative("bin");
		long c = GitIgnoreFileReader.ignoreMatchers(ip).stream().filter(pm -> {
				return pm.matches(cp);
			}
		).count();
		assertThat(c, equalTo(1L));
	}
	
	@Test
	public void tIgnoreFileOrFolder() throws IOException {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("bin"));
		assertThat(pms.size(), equalTo(1));
		tmpFolder = UtilForTe.createFileTree("bin", "a/b/c.txt");
		long count = Files.walk(tmpFolder).map(p -> tmpFolder.relativize(p)).filter(p -> {
			boolean b = pms.stream().anyMatch(m -> m.matches(p));
			return b;
			}).count();
		assertThat(count, equalTo(1L));
	}
	
	@Test
	public void tIgnoreFolder() throws IOException {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("bin/"));
		assertThat("translate to 3 matchers.", pms.size(), equalTo(3));
		tmpFolder = UtilForTe.createFileTree("bin/a/b/c.txt", "c/bin/a/b");
		long count = Files.walk(tmpFolder).map(p -> tmpFolder.relativize(p)).filter(p -> {
			boolean b = pms.stream().anyMatch(m -> m.matches(p));
			return b;
			}).count();
		assertThat(count, equalTo(4L)); // c.txt, b, a, bin
	}
	
	@Test
	public void tIgnoreNestedFolder() throws IOException {
		List<PathMatcher> pms = GitIgnoreFileReader.ignoreMatchers(Arrays.asList("**/bin/"));
		tmpFolder = UtilForTe.createFileTree("bin/a/b/c.txt", "c/bin/a/b");
		long count = Files.walk(tmpFolder).map(p -> tmpFolder.relativize(p)).filter(p -> {
			boolean b = pms.stream().anyMatch(m -> m.matches(p));
			return b;
			}).count();
		assertThat(count, equalTo(7L));
	}

}
