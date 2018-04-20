package com.go2wheel.mysqlbackup;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestMyappSettings {
	
	@Autowired
	private MyAppSettings myAppSettings;
	
	@Test
	public void t() {
		assertNotNull(myAppSettings.getSsh().getSshIdrsa());
		assertNotNull(myAppSettings.getSsh().getKnownHosts());
	}

}
