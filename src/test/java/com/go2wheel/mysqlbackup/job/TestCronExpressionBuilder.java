package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestCronExpressionBuilder {

	
	@Test
	public void t() {
		CronExpressionBuilder ceb = new CronExpressionBuilder();
		String s = ceb.hours(5, 6, 7).build();
		assertThat(s, equalTo("0 0 5,6,7 ? * *"));
		
		
		ceb = new CronExpressionBuilder();
		s = ceb.dayOfMonths(1).build();
		assertThat(s, equalTo("0 0 0 1 * ?"));
		
		
		ceb = new CronExpressionBuilder();
		s = ceb.dayOfWeeks(1).build();
		assertThat(s, equalTo("0 0 0 ? * 1"));
		
	}
}
