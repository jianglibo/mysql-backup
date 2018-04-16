package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;

public class TestPathUtil {

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
}
