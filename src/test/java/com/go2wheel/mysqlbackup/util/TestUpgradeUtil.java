package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.util.UpgradeUtil.BuildInfo;

public class TestUpgradeUtil {
	
	private Path jarFile;
	
	@Before
	public void b() throws IOException {
		Path p = Paths.get("build", "dist");
		jarFile = Files.list(p).filter(pp -> pp.toString().endsWith(".zip")).findAny().get();
	}
	
	@Test
	public void t() throws IOException {
		UpgradeUtil uu = new UpgradeUtil(jarFile);
		BuildInfo bi = uu.getBuildInfo();
		assertFalse(bi.getVersion().isEmpty());
		
		SortedMap<String, String> sm = uu.getMigs();
		
		assertThat(sm.size(), greaterThan(0));
		
	}

}
