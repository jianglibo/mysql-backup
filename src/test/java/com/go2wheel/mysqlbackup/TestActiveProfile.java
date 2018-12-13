package com.go2wheel.mysqlbackup;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

public class TestActiveProfile extends SpringBaseFort {

	@Value("${a.b.x}")
	protected int avalue;

	@Autowired
	private Environment environment;
	
	@Test
	public void classFind() throws ClassNotFoundException {
		assertThat(avalue, equalTo(123));
		assertThat(environment.getActiveProfiles().length, equalTo(1));
		assertThat(environment.getActiveProfiles()[0], equalTo("dev"));
	}
}
