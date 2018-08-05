package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;

public class TestUpgradeUtil {
	
	private Path zipFile = null;
	
	@Before
	public void b() throws IOException {
		Path p = Paths.get("testfield", "distzip");
		if (Files.exists(p)) {
			zipFile = Files.list(p).filter(pp -> pp.toString().endsWith(".zip")).findAny().orElse(null);
		}
	}
	
	@After
	public void a() throws IOException {
		Path u = Paths.get(UpgradeUtil.UPGRADE_FLAG_FILE);
		if (Files.exists(u)) {
			Files.delete(u);
		}
	}
	
	@Test
	public void tGetBuildInfoFromZip() throws IOException {
		assumeNotNull(zipFile);
		assumeTrue(Files.exists(zipFile));
		UpgradeUtil uu = new UpgradeUtil(zipFile);
		Properties bi = uu.getNewAppVersion();
		assertFalse(bi.getProperty(UpgradeUtil.BUILD_INFO_VERSION_KEY).isEmpty());
	}

	@Test
	public void tWriteUpgradeFile() throws IOException, UnExpectedInputException {
		assumeNotNull(zipFile);
		UpgradeUtil uu = new UpgradeUtil(zipFile);
		boolean writed = uu.writeUpgradeFile();
		assertFalse(writed);
		
		writed = uu.writeUpgradeFile(true);
		assertTrue(writed);
		
		UpgradeFile uf = uu.getUpgradeFile();
		String nv = uf.getNewVersion();
		String cv = uf.getCurrentVersion();
		
		int v = nv.compareTo(cv);
//		assertTrue("new-version is great than older veriosn", v > 0);
		
		assertTrue(Files.exists(Paths.get("").resolve(UpgradeUtil.UPGRADE_FLAG_FILE)));
		
		assertTrue("1.0.1".compareTo("1.0") > 0);
		
		assertTrue("2".compareTo("1.0") > 0);
		
		assertTrue("1.000012122".compareTo("2") < 0);
	}

}
