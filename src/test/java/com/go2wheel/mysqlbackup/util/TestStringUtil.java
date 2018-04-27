package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;

public class TestStringUtil {
	
	
	@Test
	public void tGetLastPartOfUrl() {
		assertThat(StringUtil.getLastPartOfUrl("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm"), equalTo("mysql57-community-release-el7-11.noarch.rpm"));
	}
	
	public void tLinuxLsl() {
		String s = "-rw-r--r--. 1 root root 659238 Apr 27 13:48 /tmp/mysqldump.sql";
		String k = "-rw-r--r--. 1 root root 659238 Apr 27 14:16 /tmp/mysqldump.sql";
		Matcher m = StringUtil.LinuxLsl.LINUX_LSL_PATTERN.matcher(s);
		s = s.trim();
		assertTrue(m.matches());
		assertThat(m.group(1), equalTo("-rw-r--r--."));
		assertThat(m.group(2), equalTo("1"));
		assertThat(m.group(3), equalTo("root"));
		assertThat(m.group(4), equalTo("root"));
		assertThat(m.group(5), equalTo("659238"));
		assertThat(m.group(6), equalTo("Apr"));
		assertThat(m.group(7), equalTo("27"));
		assertThat(m.group(8), equalTo("13:48"));
		assertThat(m.group(9), equalTo("/tmp/mysqldump.sql"));
		
		
	}

}
