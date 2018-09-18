package com.go2wheel.mysqlbackup.value;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestMysqlVersionWrapper {
	
	
	@Test
	public void test() {
		MysqlVariables.MysqlVersionWrapper mv = MysqlVariables.MysqlVersionWrapper.of("5.5.24-log");
		assertFalse(mv.isAfter55());
		
		mv = MysqlVariables.MysqlVersionWrapper.of("5.6.24-log");
		assertTrue(mv.isAfter55());

	}

}
