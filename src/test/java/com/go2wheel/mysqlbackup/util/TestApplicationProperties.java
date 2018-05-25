package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class TestApplicationProperties {
	
	@Test
	public void t() throws IOException {
		
		try (InputStream is = ClassLoader.class.getResourceAsStream("/application.properties")) {
			Properties p = new Properties();
			p.load(is);
			
			assertThat(p.getProperty("only.you"), equalTo("true"));
			
			assertThat(p.getProperty("spring.datasource.validation-query"), equalTo("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"));
			
		}
		
	
	}

}
