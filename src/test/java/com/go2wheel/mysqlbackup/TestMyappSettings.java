package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class TestMyappSettings  extends SpringBaseFort {
	
	@Test
	public void t() {
		assertNotNull(myAppSettings.getSsh().getSshIdrsa());
		assertNotNull(myAppSettings.getSsh().getKnownHosts());
		
		assertThat(env.getProperty("logging.file"), equalTo("log/spring.log"));
		assertThat(env.getProperty("logging.file.max-size"), equalTo("5MB"));
		assertThat(env.getProperty("logging.file.max-history"), equalTo("100"));
		assertThat(env.getProperty("spring.profiles.active"), equalTo("dev"));
	}
	
	@Test
	public void tappstate() {
		String[] profiles = env.getActiveProfiles();
		assertThat(profiles.length, equalTo(1));
		assertTrue("should in dev profile.", Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "dev".equals(p)));
	}

}
