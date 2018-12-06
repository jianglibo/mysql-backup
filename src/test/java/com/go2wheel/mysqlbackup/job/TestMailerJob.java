package com.go2wheel.mysqlbackup.job;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestMailerJob extends SpringBaseFort {
	
	@Autowired
	private MailerJob mailerJob;
	
	
	@Test
	public void test() {
		assertTrue(true);
	}

}
