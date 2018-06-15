package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestRemotePathUtil {
	
	@Test
	public void testgetRidOfLastSlash() {
		String l = RemotePathUtil.getRidOfLastSlash("abc///");
		assertThat(l, equalTo("abc"));
		
		l = RemotePathUtil.getRidOfLastSlash("/");
		assertThat(l, equalTo(""));
	}
	
	@Test
	public void t() {
		String s = RemotePathUtil.getParentWithEndingSlash("/var/lig/abc");
		assertThat(s, equalTo("/var/lig/"));
		
		s = RemotePathUtil.getParentWithEndingSlash("/var");
		assertThat(s, equalTo("/"));
		
		s = RemotePathUtil.getParentWithEndingSlash("var");
		assertThat(s, equalTo(""));

	}

}
