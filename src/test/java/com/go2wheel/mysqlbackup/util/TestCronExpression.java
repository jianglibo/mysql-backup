package com.go2wheel.mysqlbackup.util;

import java.text.ParseException;

import org.junit.Test;
import org.quartz.CronExpression;

public class TestCronExpression {
	
	@Test(expected=ParseException.class)
	public void tAllAsterisk() throws ParseException {
		new CronExpression("* * * * * * *");
	}
	
	@Test(expected=ParseException.class)
	public void tBigRange() throws ParseException {
		new CronExpression("0/70 * * * * ? *");
	}
	
	@Test
	public void tZeroRange() throws ParseException {
		new CronExpression("0/0 * * * * ? *");
	}

}
