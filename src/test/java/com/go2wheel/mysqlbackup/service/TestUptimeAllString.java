package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.service.ServerStateService.UptimeAllString;

public class TestUptimeAllString {
	
	@Test
	public void t() {
		List<String> ls = Arrays.asList("2018-05-19 20:41:51", "up 3 hours, 6 minutes", " 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05");
		UptimeAllString ut = UptimeAllString.build(ls);
		assertThat(ut.getSince(), equalTo("2018-05-19 20:41:51"));
		assertThat(ut.getUptime(), equalTo("3 hours, 6 minutes"));
		assertThat(ut.getLoadOne(), equalTo("0.00"));
		assertThat(ut.getLoadFive(), equalTo("0.01"));
		assertThat(ut.getLoadFifteen(), equalTo("0.05"));
	}
	
	@Test
	public void tToUpTime() {
		List<String> ls = Arrays.asList("2018-05-19 20:41:51", "up 3 hours, 6 minutes", " 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05");
		UptimeAllString uta = UptimeAllString.build(ls);
		UpTime ut = uta.toUpTime();
		
		assertThat(ut.getLoadOne(), equalTo(0));
		assertThat(ut.getLoadFive(), equalTo(1));
		assertThat(ut.getLoadFifteen(), equalTo(5));
		assertThat(ut.getUptimeMinutes(), equalTo(186));
		
		
		ls = Arrays.asList("2018-05-19 20:41:51", "up 3 hours, 6", " 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05");
		uta = UptimeAllString.build(ls);
		ut = uta.toUpTime();
		
		assertThat(ut.getUptimeMinutes(), equalTo(180));
		
		ls = Arrays.asList("2018-05-19 20:41:51", "up 3 hours minutes , 6", " 23:48:46 up  3:06,  1 user,  load average: 0.00, 0.01, 0.05");
		uta = UptimeAllString.build(ls);
		ut = uta.toUpTime();
		
		assertThat(ut.getUptimeMinutes(), equalTo(3));


	}

}
