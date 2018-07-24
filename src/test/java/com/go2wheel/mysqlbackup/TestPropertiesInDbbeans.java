package com.go2wheel.mysqlbackup;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import(com.go2wheel.mysqlbackup.TestPropertiesInDbbeans.Tcc.class)
public class TestPropertiesInDbbeans extends SpringBaseFort {

	@Autowired
	@Qualifier("propertiesInDb")
	private Properties pps;

	@Test
	public void thymeLeafResolver() {
		assertNotNull(pps);
	}
	
	@TestConfiguration
	public static class Tcc {
		
	}


}
