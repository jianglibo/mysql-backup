package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestDefaultValue extends SpringBaseFort {
	
	@Autowired
	private DefaultValues defaultValues;
	
	@Test
	public void tValue() {
		assertNotNull(defaultValues.getCron().getBorgArchive());
		assertThat(defaultValues.getCron().getCommon().size(), equalTo(10));
	}
	
	@Test
	public void testDefaultCount() {
		KeyValueProperties kvp = defaultValues.getDefaultCount();
		assertThat(kvp.getInteger(DefaultValues.BORG_DOWNLOAD_CN), equalTo(20));
		assertThat(kvp.getInteger(DefaultValues.JOB_LOG_CN), equalTo(20));
		assertThat(kvp.getInteger(DefaultValues.MYSQL_DUMP_CN), equalTo(2));
		assertThat(kvp.getInteger(DefaultValues.MYSQL_FLUSH_CN), equalTo(10));
		assertThat(kvp.getInteger(DefaultValues.SERVER_STATE_CN), equalTo(2));
		assertThat(kvp.getInteger(DefaultValues.STORAGE_STATE_CN), equalTo(7));
	}

}
