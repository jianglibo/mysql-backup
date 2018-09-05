package com.go2wheel.mysqlbackup.robocopy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestWinrar {
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
    
	private Path createALocalFile(Path tmpFile, String content) throws IOException {
		Path parent = tmpFile.toAbsolutePath().getParent();
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}
		Files.write(tmpFile, content.getBytes());
		return tmpFile;
	}

    
	private Path createDemoSrc() throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}
	
	@Test
	public void t() throws IOException {
		createDemoSrc();
		Path src = srcfolder.getRoot().toPath().toAbsolutePath();
		Path p = Paths.get("a");
		assertNull(p.getRoot());
		assertNotNull(src.getRoot());
		String r = src.getRoot().toString(); //c:\
		assertTrue(r.contains(":"));
		Path rr = Paths.get(r).relativize(src);
		assertNull(rr.getRoot());
	}

}
