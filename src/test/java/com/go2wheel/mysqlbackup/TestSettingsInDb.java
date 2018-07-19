package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestSettingsInDb extends SpringBaseFort {
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Before
	public void af() {
		settingsInDb.getLc().invalidateAll();
	}
	
	@Test
	public void testGetString() {
		clearDb();
		String v = settingsInDb.getString("abc");
		assertThat(v, equalTo(""));
	}
	
	@Test
	public void testGetInt() {
		clearDb();
		int v = settingsInDb.getInteger("abc");
		assertThat(v, equalTo(0));
	}
	@Test
	public void testGetStringDefault() {
		clearDb();
		String v = settingsInDb.getString("abc", "hello");
		assertThat(v, equalTo("hello"));
	}
	
	@Test
	public void testGetIntDefault() {
		clearDb();
		int v = settingsInDb.getInteger("abc", 66);
		assertThat(v, equalTo(66));
	}


}
