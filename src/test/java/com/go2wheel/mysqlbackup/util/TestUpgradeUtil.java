package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.util.UpgradeUtil.BuildInfo;
import com.go2wheel.mysqlbackup.util.UpgradeUtil.UpgradeFile;

public class TestUpgradeUtil {
	
	private Path zipFile = null;
	
	@Before
	public void b() throws IOException {
		Path p = Paths.get("build", "dist");
		if (Files.exists(p)) {
			zipFile = Files.list(p).filter(pp -> pp.toString().endsWith(".zip")).findAny().orElse(null);
		}
		
	}
	
	@Test
	public void t() throws IOException {
		assumeNotNull(zipFile);
		assumeTrue(Files.exists(zipFile));
		UpgradeUtil uu = new UpgradeUtil(zipFile);
		BuildInfo bi = uu.getBuildInfo();
		assertFalse(bi.getVersion().isEmpty());
		SortedMap<String, String> sm = uu.getMigs();
		assertThat(sm.size(), greaterThan(0));
	}

	@Test
	public void twriteUpgradeFile() throws IOException {
		assumeNotNull(zipFile);
		UpgradeUtil uu = new UpgradeUtil(zipFile);
		uu.writeUpgradeFile();
		UpgradeFile uf = uu.getUpgradeFilẹ();
		String nv = uf.getNewVersion();
		String cv = uf.getCurrentVersion();
		int v = nv.compareTo(cv);
		assertTrue("new-version is great than older veriosn", v > 0);
		
		assertTrue(Files.exists(Paths.get("").resolve(UpgradeUtil.UPGRADE_FLAG_FILE)));
		
		assertTrue("1.0.1".compareTo("1.0") > 0);
		
		assertTrue("2".compareTo("1.0") > 0);
		
		assertTrue("1.000012122".compareTo("2") < 0);
	}

}
