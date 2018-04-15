package com.go2wheel.mysqlbackup.valueprovider;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.valueprovider.FileValueProviderMine;

public class TestFileValueProviderMine {
	
	@Test
	public void tEmpty() {
		CompletionContext cc = new CompletionContext(Arrays.asList("-"), 0, 1);
		FileValueProviderMine fvpm = new FileValueProviderMine();
		List<CompletionProposal> lcp = fvpm.complete(null, cc, new String[] {});
		assertThat("should be empty", lcp.size(), equalTo(0));
	}

	@Test
	public void tTwoDotAndSlash() throws IOException {
		CompletionContext cc = new CompletionContext(Arrays.asList("../"), 0, 3);
		FileValueProviderMine fvpm = new FileValueProviderMine();
		Path d = Paths.get("..").toAbsolutePath();
		UtilForTe.printme(d);
		UtilForTe.printme(Files.list(d).count());
		List<CompletionProposal> lcp = fvpm.complete(null, cc, new String[] {});
		assertThat("should not be empty", lcp.size(), greaterThan(0));
	}
	
	@Test
	public void tTwoDot() throws IOException {
		CompletionContext cc = new CompletionContext(Arrays.asList(".."), 0, 2);
		FileValueProviderMine fvpm = new FileValueProviderMine();
		Path d = Paths.get("..").toAbsolutePath();
		List<CompletionProposal> lcp = fvpm.complete(null, cc, new String[] {});
		long count = lcp.stream().map(c -> c.value().startsWith("../")).filter(t -> t).count();
		assertThat("only one item start with '..'", (int)count, equalTo(lcp.size() - 1));
		assertThat("should not be empty.", lcp.size(), greaterThan(0));
	}
	
	@Test
	public void tNoneExists() {
		CompletionContext cc = new CompletionContext(Arrays.asList("ab/"), 0, 3);
		FileValueProviderMine fvpm = new FileValueProviderMine();
		List<CompletionProposal> lcp = fvpm.complete(null, cc, new String[] {});
		assertThat("should be empty", lcp.size(), equalTo(0));
	}

}
