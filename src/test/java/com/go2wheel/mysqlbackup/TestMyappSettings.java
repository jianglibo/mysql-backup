package com.go2wheel.mysqlbackup;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest({"spring.shell.interactive.enabled=false"})
@RunWith(SpringRunner.class)
public class TestMyappSettings {
	
	@Autowired
	private MyAppSettings myAppSettings;
	
	@Autowired
	private Environment env;
	
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
		assertFalse(ApplicationState.IS_PROD_MODE);
	}

}
