package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestStringUtil {
	
	
	@Test
	public void tGetLastPartOfUrl() {
		assertThat(StringUtil.getLastPartOfUrl("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm"), equalTo("mysql57-community-release-el7-11.noarch.rpm"));
	}

}
